package telran.accounting.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
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
import telran.accounting.kafka.KafkaProducer;
import telran.accounting.dao.ProfileCustomRepository;
import telran.accounting.dao.ProfileRepository;
import telran.accounting.dto.*;
import telran.accounting.dto.exceptions.ProfileExistsException;
import telran.accounting.model.*;
import telran.accounting.security.JwtTokenService;
import telran.accounting.security.UserDetailsServiceImpl;

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
    private final UserDetailsServiceImpl userDetailsService;

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
        EducationLevel education = Arrays.stream(EducationLevel.values())
                .filter(e -> e.name().equalsIgnoreCase(newProfile.getEducationLevel().replace("_", " ")))
                .findFirst()
                .orElse(EducationLevel.OTHER);
        profile.setEducationLevel(education);
        profile.calculateRating();
        profile.setPassword(passwordEncoder.encode(profile.getPassword()));
        profile.setEmail(encryptedEmail);
        profileRepository.save(profile);
        jwtTokenService.generateToken(profile);
        String token = jwtTokenService.getCurrentProfileToken(profile.getEmail());
        kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> logInProfile(String profileId) {
        Profile profile = findProfileOrThrowError(profileId);
        String token = jwtTokenService.getCurrentProfileToken(profileId);
        kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean logOutProfile() {
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getName());
        String curProfile = SecurityContextHolder.getContext().getAuthentication().getName();
        jwtTokenService.deleteCurrentProfileToken(curProfile);
        SecurityContextHolder.getContext().setAuthentication(null);
        kafkaProducer.setProfile(new ProfileDto());
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileDto getProfile(String profileId) {
        Profile profile = findProfileOrThrowError(profileId);
        return modelMapper.map(profile, ProfileDto.class);
    }

    @Override
    public Set<ProfileDto> getProfilesByCommunities(Set<String> communities) {
        return profileRepository.findAllByCommunitiesContaining(communities)
                .map(p -> modelMapper.map(p, ProfileDto.class))
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<ProfileDto> getProfiles() {
        return profileRepository.findAll().stream().map(e -> modelMapper.map(e, ProfileDto.class)).collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public ProfileDto editName(String profileId, NameDto newName) {
        Profile profile = findProfileOrThrowError(profileId);
        profile.setUsername(newName.getUsername());
        profileRepository.save(profile);
        ProfileDto profileDto = modelMapper.map(profile, ProfileDto.class);
        kafkaProducer.setProfile(profileDto);
        return profileDto;
    }

    @Override
    @Transactional
    public ProfileDto editEducation(String profileId, String newEducation) {
        Profile profile = findProfileOrThrowError(profileId);
        if (Arrays.stream(EducationLevel.values()).noneMatch(e -> e.toString().equalsIgnoreCase(newEducation))) {
            profile.setEducationLevel(EducationLevel.OTHER);
            profile.calculateRating();
        } else {
            profile.setEducationLevel(EducationLevel.valueOf(newEducation.toUpperCase()));
            profile.calculateRating();
        }
        profileRepository.save(profile);
        return modelMapper.map(profile, ProfileDto.class);
    }

    @Override
    @Transactional
    public ProfileDto editCommunities(String profileId, CommunitiesDto newCommunities) {
        Profile profile = findProfileOrThrowError(profileId);
        profile.editCommunities(newCommunities.getCommunities());
        profileRepository.save(profile);
        return modelMapper.map(profile, ProfileDto.class);
    }

    @Override
    @Transactional
    public ProfileDto editLocation(String profileId, LocationDto newLocation) {
        Profile profile = findProfileOrThrowError(profileId);
        profile.setLocation(modelMapper.map(newLocation, Location.class));
        profileRepository.save(profile);
        return modelMapper.map(profile, ProfileDto.class);
    }

    @Override
    @Transactional
    public ProfileDto editAvatar(String profileId, String newAvatar) {
        Profile profile = findProfileOrThrowError(profileId);
        profile.setAvatar(newAvatar);
        profileRepository.save(profile);
        return modelMapper.map(profile, ProfileDto.class);
    }

    @Override
    @Transactional
    public Boolean editPassword(String profileId, String newPassword) {
        try {
            Profile profile = findProfileOrThrowError(profileId);
            String hashedPassword = passwordEncoder.encode(newPassword);
            profile.setPassword(hashedPassword);
            profileRepository.save(profile);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

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

    @Override
    @Transactional
    public ProfileDto deleteUser(String profileId) {
        Profile profile = findProfileOrThrowError(profileId);
        ProfileDto profileDto = modelMapper.map(profile, ProfileDto.class);
        profileDto.setUsername("DELETED_PROFILE");
        kafkaProducer.setProfile(profileDto);
        removeAllAuthorsActivities(profile);
        profileRepository.deleteById(profileId);
        return profileDto;
    }

    //Administrative methods//
    @Override
    @Transactional
    public ProfileDto editRole(String profileId, String targetId, RoleDto role) {
        Profile adminProfile = findProfileOrThrowError(profileId);
        Profile profile = findProfileOrThrowError(targetId);
        if (adminProfile.getRoles().contains(Roles.ADMINISTRATOR)) {
            profile.setRoles(Roles.convertFromDto(role.getRoles()));
            profileRepository.save(profile);
        }
        return modelMapper.map(profile, ProfileDto.class);
    }

    @Override
    public ProfileDto deleteUser(String profileId, String targetId) {
        Profile adminProfile = findProfileOrThrowError(profileId);
        Profile targetProfile = findProfileOrThrowError(targetId);
        if (adminProfile.getRoles().contains(Roles.ADMINISTRATOR)) {
            ProfileDto profileDto = modelMapper.map(targetProfile, ProfileDto.class);
            profileDto.setUsername("DELETED_PROFILE");
            kafkaProducer.setProfile(profileDto);
            removeAllAuthorsActivities(targetProfile);
            profileRepository.deleteById(targetId);
            return profileDto;
        } else throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "You have no permissions to delete that user");
    }

    @Override
    public ProfileDto deleteAvatar(String profileId, String targetId) {
        Profile adminProfile = findProfileOrThrowError(profileId);
        Profile targetProfile = findProfileOrThrowError(targetId);
        if (adminProfile.getRoles().contains(Roles.ADMINISTRATOR)) {
            targetProfile.setAvatar("");
            profileRepository.save(targetProfile);
            return modelMapper.map(targetProfile, ProfileDto.class);
        } else
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "You have no permissions to delete users avatar");
    }

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

    private Profile findProfileOrThrowError(String profileId) {
        return profileRepository.findById(profileId).orElseThrow(NoSuchElementException::new);
    }

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
