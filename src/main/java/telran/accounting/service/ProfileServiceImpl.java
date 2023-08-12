package telran.accounting.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import telran.accounting.configuration.EmailEncryptionConfiguration;
import telran.accounting.configuration.KafkaProducer;
import telran.accounting.dao.ProfileRepository;
import telran.accounting.dto.*;
import telran.accounting.model.*;
import telran.accounting.model.exceptions.ProfileExistsException;

import java.util.*;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService, CommandLineRunner {

    final ProfileRepository profileRepository;
    final ModelMapper modelMapper;
    final PasswordEncoder passwordEncoder;
    final JavaMailSender javaMailSender;
    final KafkaProducer kafkaProducer;


    @Override
    @Transactional
    public ProfileDto addProfile(RegisterProfileDto newProfile) {
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
        if (profile.getEducationLevel() == null) {
            profile.setEducationLevel(EducationLevel.OTHER);
        }
        String hashedPassword = passwordEncoder.encode(profile.getPassword());
        profile.setPassword(hashedPassword);
        profile.setEmail(encryptedEmail);
        profile.editStats(String.valueOf(profile.getEducationLevel()));
        profile.setRoles(Set.of(Roles.USER));
        profileRepository.save(profile);
        return modelMapper.map(profile, ProfileDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileDto getProfile(String profileId) {
        //How to get authentication information about logged user.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String currentUsername = authentication.getName();
            Profile profile = findProfileOrThrowError(profileId);
            System.out.println("Request from repository: " + profile.getEmail());
            System.out.println("Request from user: " + currentUsername);

            kafkaProducer.setMessage(currentUsername);
            kafkaProducer.send().get();



        }
        Profile profile = findProfileOrThrowError(profileId);
        return modelMapper.map(profile, ProfileDto.class);
    }

    @Override
    @Transactional
    public ProfileDto editName(String profileId, NameDto newName) {
        Profile profile = findProfileOrThrowError(profileId);
        profile.setUsername(newName.getUsername());
        profileRepository.save(profile);
        return modelMapper.map(profile, ProfileDto.class);
    }

    @Override
    @Transactional
    public ProfileDto editEducation(String profileId, String newEducation) {
        Profile profile = findProfileOrThrowError(profileId);
        if (Arrays.stream(EducationLevel.values()).noneMatch(e -> e.toString().equalsIgnoreCase(newEducation))) {
            profile.setEducationLevel(EducationLevel.OTHER);
        } else {
            profile.setEducationLevel(EducationLevel.valueOf(newEducation.toUpperCase()));
        }
        profile.editStats(newEducation);
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
        }catch (Exception e){
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
        profileRepository.deleteById(profileId);
        return modelMapper.map(profile, ProfileDto.class);
    }

    //Administrative methods//
    @Override
    @Transactional
    public ProfileDto editRole(String profileId,String targetId, RoleDto role) {
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
            profileRepository.deleteById(targetId);
        }
        return modelMapper.map(targetProfile, ProfileDto.class);
    }

    @Override
    public ProfileDto deleteAvatar(String profileId, String targetId) {
        Profile adminProfile = findProfileOrThrowError(profileId);
        Profile targetProfile = findProfileOrThrowError(targetId);
        if (adminProfile.getRoles().contains(Roles.ADMINISTRATOR)) {
            targetProfile.setAvatar("");
            profileRepository.save(targetProfile);
        }
        return modelMapper.map(targetProfile, ProfileDto.class);
    }

    private Profile findProfileOrThrowError(String profileId) {
        return profileRepository.findById(profileId).orElseThrow(NoSuchElementException::new);
    }

    @Override
    public void run(String... args) throws Exception {
        if (!profileRepository.existsByRolesContaining(Roles.ADMINISTRATOR.name())) {
            String password = BCrypt.hashpw("admin", BCrypt.gensalt());
            String email = EmailEncryptionConfiguration.encryptAndEncodeUserId("adminemail@mail.com");
            Profile adminProfile = new Profile("admin", email, EducationLevel.OTHER, new HashSet<>(), new Location(), password, Set.of(Roles.ADMINISTRATOR, Roles.MODERATOR, Roles.USER), "", new Stats(), new HashSet<Activity>(), 0.);
            profileRepository.save(adminProfile);
        }
    }
}
