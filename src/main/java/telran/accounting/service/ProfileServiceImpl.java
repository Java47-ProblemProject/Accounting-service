package telran.accounting.service;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import telran.accounting.configuration.EmailEncryptionConfiguration;
import telran.accounting.dao.ProfileCustomRepository;
import telran.accounting.dao.ProfileRepository;
import telran.accounting.dto.*;
import telran.accounting.dto.exceptions.ProfileExistsException;
import telran.accounting.kafka.KafkaProducer;
import telran.accounting.kafka.kafkaDataDto.profileDataDto.ProfileDataDto;
import telran.accounting.kafka.kafkaDataDto.profileDataDto.ProfileMethodName;
import telran.accounting.model.*;
import telran.accounting.security.JwtTokenService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService, CommandLineRunner {
    private final ProfileRepository profileRepository;
    private final ProfileCustomRepository profileCustomRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;
    private final KafkaProducer kafkaProducer;
    private final JwtTokenService jwtTokenService;

    /**
     * Adds a new user profile to the system and generates a JWT token for authentication.
     *
     * @param newProfile A RegisterProfileDto containing the information of the new profile to be added.
     * @return A map containing the generated JWT token with the key "token."
     * @throws RuntimeException       If there is an error in encrypting the email or generating the token.
     * @throws ProfileExistsException If a profile with the same email already exists.
     */
    @Override
    @Transactional
    public Map<String, String> addProfile(RegisterProfileDto newProfile) {
        String encryptedEmail;
        try {
            encryptedEmail = EmailEncryptionConfiguration.encryptAndEncodeUserId(newProfile.getEmail());
        } catch (Exception e) {
            throw new RuntimeException();
        }
        if (profileRepository.existsById(encryptedEmail)) {
            throw new ProfileExistsException();
        }
        Profile profile = modelMapper.map(newProfile, Profile.class);
        profile.setEducationLevel(
                Arrays.stream(EducationLevel.values())
                        .filter(e -> e.name().equalsIgnoreCase(newProfile.getEducationLevel().replace(" ", "_")))
                        .findFirst()
                        .orElse(EducationLevel.OTHER)
        );
        profile.calculateRating();
        profile.setPassword(passwordEncoder.encode(profile.getPassword()));
        profile.setEmail(encryptedEmail);
        profileRepository.save(profile);
        jwtTokenService.generateToken(profile);
        String token = jwtTokenService.getCurrentProfileToken(profile.getEmail());
        transferData(profile, token, ProfileMethodName.SET_PROFILE);
        return Collections.singletonMap("token", token);
    }

    /**
     * Logs in a user profile and generates a JWT token for authentication.
     *
     * @param profileId The unique identifier of the user's profile to log in.
     * @return A map containing the generated JWT token with the key "token."
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, String> logInProfile(String profileId) {
        Profile profile = findProfileOrThrowError(profileId);
        String token = jwtTokenService.getCurrentProfileToken(profileId);
        transferData(profile, token, ProfileMethodName.SET_PROFILE);
        return Collections.singletonMap("token", token);
    }

    /**
     * Logs out the currently authenticated user profile by deleting the associated JWT token and clearing the authentication context.
     *
     * @return `true` if the logout is successful.
     */
    @Override
    @Transactional(readOnly = true)
    public Boolean logOutProfile() {
        String curProfile = SecurityContextHolder.getContext().getAuthentication().getName();
        jwtTokenService.deleteCurrentProfileToken(curProfile);
        SecurityContextHolder.getContext().setAuthentication(null);
        transferData(new Profile(), "", ProfileMethodName.UNSET_PROFILE);
        return true;
    }

    /**
     * Retrieves a user profile by its unique identifier and optionally decrypts the email address for the authenticated profile.
     *
     * @param profileId The unique identifier of the user's profile to retrieve.
     * @return A ProfileDto representing the user's profile with an optionally decrypted email address.
     * @throws RuntimeException If there is an error in decrypting the email address.
     */
    @Override
    @Transactional(readOnly = true)
    public ProfileDto getProfile(String profileId) {
        Profile profile = findProfileOrThrowError(profileId);
        String authProfileEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (authProfileEmail.equals(profileId)) {
            try {
                String decryptedEmail = EmailEncryptionConfiguration.decryptAndDecodeUserId(authProfileEmail);
                profile.setEmail(decryptedEmail);
            } catch (Exception e) {
                throw new RuntimeException();
            }
        }
        return modelMapper.map(profile, ProfileDto.class);
    }

    /**
     * Retrieves a set of user profiles based on the specified communities.
     *
     * @param communities The set of communities to filter profiles by.
     * @return A set of ProfileDto representing user profiles matching the specified communities.
     */
    @Override
    @Transactional(readOnly = true)
    public Set<ProfileDto> getProfilesByCommunities(Set<String> communities) {
        return profileRepository.findAllByCommunitiesContaining(communities)
                .map(p -> modelMapper.map(p, ProfileDto.class))
                .collect(Collectors.toSet());
    }

    /**
     * Retrieves a set of user profiles ordered by descending rating.
     *
     * @return A set of ProfileDto representing user profiles, ordered by rating.
     */
    @Override
    @Transactional(readOnly = true)
    public Set<ProfileDto> getProfiles() {
        return profileRepository.findAllByOrderByStats_RatingDesc()
                .map(p -> modelMapper.map(p, ProfileDto.class))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Retrieves the email address of a user profile after decrypting and decoding it.
     *
     * @param profileId The unique identifier of the user's profile.
     * @return A ResponseEntity containing the decrypted and decoded email address, or an error response if decryption fails.
     * @throws RuntimeException If there is an error in decrypting or decoding the email address.
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<String> getEmail(String profileId) {
        String email = findProfileOrThrowError(profileId).getEmail();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(EmailEncryptionConfiguration.decryptAndDecodeUserId(email), headers, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    /**
     * Edits the username of a user profile.
     *
     * @param profileId The unique identifier of the user's profile.
     * @param newName   A NameDto containing the new username to set for the user.
     * @return A ProfileDto representing the user's profile after the username edit.
     */
    @Override
    @Transactional
    public ProfileDto editName(String profileId, NameDto newName) {
        Profile profile = findProfileOrThrowError(profileId);
        profile.setUsername(newName.getUsername());
        profileRepository.save(profile);
        ProfileDto profileDto = modelMapper.map(profile, ProfileDto.class);
        transferData(profile, "", ProfileMethodName.EDIT_PROFILE_NAME);
        return profileDto;
    }

    /**
     * Edits the education level of a user profile.
     *
     * @param profileId    The unique identifier of the user's profile.
     * @param newEducation The new education level to set for the user.
     * @return A ProfileDto representing the user's profile after the education level edit.
     */
    @Override
    @Transactional
    public ProfileDto editEducation(String profileId, String newEducation) {
        Profile profile = findProfileOrThrowError(profileId);
        String educationLevel = newEducation.toUpperCase().replace(" ", "_");
        if (Arrays.stream(EducationLevel.values()).noneMatch(e -> e.toString().equalsIgnoreCase(educationLevel))) {
            profile.setEducationLevel(EducationLevel.OTHER);
            profile.calculateRating();
        } else {
            profile.setEducationLevel(EducationLevel.valueOf(educationLevel));
            profile.calculateRating();
        }
        transferData(profile, "", ProfileMethodName.EDIT_PROFILE_EDUCATION);
        profileRepository.save(profile);
        return modelMapper.map(profile, ProfileDto.class);
    }

    /**
     * Edits the list of communities associated with a user profile.
     *
     * @param profileId      The unique identifier of the user's profile.
     * @param newCommunities A CommunitiesDto containing the updated list of communities.
     * @return A ProfileDto representing the user's profile after the communities edit.
     */
    @Override
    @Transactional
    public ProfileDto editCommunities(String profileId, CommunitiesDto newCommunities) {
        Profile profile = findProfileOrThrowError(profileId);
        profile.editCommunities(newCommunities.getCommunities());
        profileRepository.save(profile);
        transferData(profile, "", ProfileMethodName.EDIT_PROFILE_COMMUNITIES);
        return modelMapper.map(profile, ProfileDto.class);
    }

    /**
     * Edits the location of a user profile.
     *
     * @param profileId   The unique identifier of the user's profile.
     * @param newLocation The new Location data to set for the user.
     * @return A ProfileDto representing the user's profile after the location edit.
     */
    @Override
    @Transactional
    public ProfileDto editLocation(String profileId, LocationDto newLocation) {
        Profile profile = findProfileOrThrowError(profileId);
        profile.setLocation(modelMapper.map(newLocation, Location.class));
        profileRepository.save(profile);
        return modelMapper.map(profile, ProfileDto.class);
    }

    /**
     * Edits the avatar of a user profile.
     *
     * @param profileId The unique identifier of the user's profile.
     * @param newAvatar The new avatar image URL or data to set for the user.
     * @return A ProfileDto representing the user's profile after the avatar edit.
     */
    @Override
    @Transactional
    public ProfileDto editAvatar(String profileId, String newAvatar) {
        Profile profile = findProfileOrThrowError(profileId);
        profile.setAvatar(newAvatar);
        profileRepository.save(profile);
        return modelMapper.map(profile, ProfileDto.class);
    }

    /**
     * Edits the password of a user profile.
     *
     * @param profileId   The unique identifier of the user's profile.
     * @param newPassword The new password to set for the user.
     * @return `true` if the password is successfully edited and the user is logged out; `false` if the profile with the given ID is not found.
     */
    @Override
    @Transactional
    public Boolean editPassword(String profileId, String newPassword) {
        try {
            Profile profile = findProfileOrThrowError(profileId);
            String hashedPassword = passwordEncoder.encode(newPassword);
            profile.setPassword(hashedPassword);
            profileRepository.save(profile);
            return logOutProfile();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Resets the password for a user account and sends the new password to the user's email address.
     *
     * @param emailAddress The email address associated with the user account.
     * @return `true` if the password reset was successful; otherwise, an exception is thrown.
     * @throws RuntimeException If there is an error in resetting the password or sending the email.
     */
    @Override
    public Boolean resetPassword(String emailAddress) {
        String encryptedEmail;
        try {
            encryptedEmail = EmailEncryptionConfiguration.encryptAndEncodeUserId(emailAddress);
        } catch (Exception e) {
            throw new RuntimeException();
        }
        Profile profile = findProfileOrThrowError(encryptedEmail);
        String newPassword = new Base64StringKeyGenerator().generateKey();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailAddress);
        message.setSubject("JAN new password");
        message.setText("Your new password is:\n\n" + newPassword + "\n\nPlease remember to change it once you log in for the first time.");
        javaMailSender.send(message);
        newPassword = passwordEncoder.encode(newPassword);
        profile.setPassword(newPassword);
        profileRepository.save(profile);
        return true;
    }

    /**
     * Deletes a user profile by an owner.
     *
     * @param profileId The unique identifier of the administrator's profile.
     * @return A ProfileDto representing the deleted user's profile with the username set to "DELETED_PROFILE".
     */
    @Override
    @Transactional
    public ProfileDto deleteUser(String profileId) {
        return getDeletedProfileDto(profileId);
    }

    //Administrative methods//

    /**
     * Edits the roles of a user profile by an administrator.
     *
     * @param profileId The unique identifier of the administrator's profile.
     * @param targetId  The unique identifier of the user's profile whose roles are to be edited.
     * @param role      A RoleDto containing the new roles for the user.
     * @return A ProfileDto representing the user's profile after the role edit.
     */
    @Override
    @Transactional
    public ProfileDto editRole(String profileId, String targetId, RoleDto role) {
        Profile profile = findProfileOrThrowError(targetId);
        profile.setRoles(Roles.convertFromDto(role.getRoles()));
        profileRepository.save(profile);
        return modelMapper.map(profile, ProfileDto.class);
    }

    /**
     * Deletes a user profile by an administrator.
     *
     * @param profileId The unique identifier of the administrator's profile.
     * @param targetId  The unique identifier of the user's profile to be deleted.
     * @return A ProfileDto representing the deleted user's profile with the username set to "DELETED_PROFILE".
     */
    @Override
    public ProfileDto deleteUser(String profileId, String targetId) {
        return getDeletedProfileDto(targetId);
    }

    /**
     * Deletes the avatar of a user profile by an administrator.
     *
     * @param targetId The unique identifier of the target user's profile whose avatar is to be deleted.
     * @return A ProfileDto representing the target user's profile after the avatar deletion.
     * @throws HttpClientErrorException If the administrator does not have permission to delete the user's avatar (FORBIDDEN status).
     */
    @Override
    public ProfileDto deleteAvatar(String profileId, String targetId) {
        Profile targetProfile = findProfileOrThrowError(targetId);
        targetProfile.setAvatar("");
        profileRepository.save(targetProfile);
        return modelMapper.map(targetProfile, ProfileDto.class);
    }

    /**
     * Retrieves a list of education levels as strings.
     *
     * @return A list of education levels, where each level is formatted as a user-friendly string.
     */
    @Override
    public List<String> getEducationList() {
        return Arrays.stream(EducationLevel.values())
                .map(l -> {
                    String educationLevel = l.name().toLowerCase().replace("_", " ");
                    educationLevel = educationLevel.substring(0, 1).toUpperCase() + educationLevel.substring(1);
                    return educationLevel;
                })
                .collect(Collectors.toList());
    }

    /**
     * Finds a profile by its unique identifier or throws a NoSuchElementException if not found.
     *
     * @param profileId The unique identifier of the profile to find.
     * @return The found profile.
     * @throws NoSuchElementException If the profile with the given ID does not exist.
     */
    private Profile findProfileOrThrowError(String profileId) {
        return profileRepository.findById(profileId).orElseThrow(NoSuchElementException::new);
    }

    /**
     * Generates a ProfileDto representation for a deleted user profile, performs necessary cleanup,
     * and deletes the profile record from the repository.
     *
     * @param targetId The unique identifier of the deleted user's profile.
     * @return A ProfileDto representing the deleted user's profile with the username set to "DELETED_PROFILE".
     */
    @NotNull
    private ProfileDto getDeletedProfileDto(String targetId) {
        Profile targetProfile = findProfileOrThrowError(targetId);
        ProfileDto profileDto = modelMapper.map(targetProfile, ProfileDto.class);
        profileDto.setUsername("DELETED_PROFILE");
        transferData(targetProfile, "", ProfileMethodName.DELETE_PROFILE);
        removeAllAuthorsActivities(targetProfile);
        profileRepository.deleteById(targetId);
        return profileDto;
    }

    /**
     * Transfers data related to a profile and a JWT token(only for sign up/sign in methods) to a Kafka producer.
     *
     * @param profile    The profile for which data is being transferred.
     * @param token      The JWT token (may be empty).
     * @param methodName The name of the method triggering the transfer.
     */
    private void transferData(Profile profile, String token, ProfileMethodName methodName) {
        ProfileDataDto profileData = token.isEmpty() ? new ProfileDataDto(profile.getUsername(), profile.getEmail(), profile.getStats().getRating(), profile.getCommunities(), profile.getActivities(), methodName)
                : new ProfileDataDto(token, profile.getUsername(), profile.getEmail(), profile.getStats().getRating(), profile.getCommunities(), profile.getActivities(), methodName);
        kafkaProducer.setProfile(profileData);
    }

    /**
     * Removes all activities authored by the profile.
     *
     * @param profile The profile for which authored activities are being removed.
     */
    private void removeAllAuthorsActivities(Profile profile) {
        Set<String> profileAuthoredActivities = profile.getActivities()
                .entrySet()
                .stream()
                .filter(e -> e.getValue().getAction().contains("AUTHOR")
                        && (e.getValue().getType().equals("PROBLEM") || e.getValue().getType().equals("COMMENT") || e.getValue().getType().equals("SOLUTION")))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        profileCustomRepository.removeKeysFromActivity(profileAuthoredActivities);
    }

    @Override
    public void run(String... args) throws Exception {
        if (!profileRepository.existsByRolesContaining(Roles.ADMINISTRATOR.name())) {
            String password = BCrypt.hashpw("admin", BCrypt.gensalt());
            String email = EmailEncryptionConfiguration.encryptAndEncodeUserId("adminemail@mail.com");
            Profile adminProfile = new Profile("admin", email, EducationLevel.OTHER, new HashSet<>(), new Location(),
                    password, Set.of(Roles.ADMINISTRATOR, Roles.MODERATOR, Roles.USER), "", new Stats(), new HashMap<>(), 0.);
            profileRepository.save(adminProfile);
        }
    }
}
