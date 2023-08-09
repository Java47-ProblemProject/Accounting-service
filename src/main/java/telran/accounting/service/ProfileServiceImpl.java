package telran.accounting.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import telran.accounting.configuration.EmailEncryptionUtils;
import telran.accounting.dao.ProfileRepository;
import telran.accounting.dto.*;
import telran.accounting.model.*;
import telran.accounting.model.exceptions.ProfileExistsException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService, CommandLineRunner {

    final ProfileRepository profileRepository;
    final ModelMapper modelMapper;
    final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ProfileDto addProfile(RegisterProfileDto newProfile) {
        String encryptedEmail;
        try {
            encryptedEmail = EmailEncryptionUtils.encryptAndEncodeUserId(newProfile.getEmail());
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
        profile.editRole(Roles.USER);
        profileRepository.save(profile);
        return modelMapper.map(profile, ProfileDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileDto getProfile(String profileId) {
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
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    @Override
    @Transactional
    public ProfileDto deleteUser(String profileId) {
        Profile profile = findProfileOrThrowError(profileId);
        profileRepository.deleteById(profileId);
        return modelMapper.map(profile, ProfileDto.class);
    }

    @Override
    @Transactional
    public ProfileDto editRole(String profileId, Roles role) {
        Profile profile = findProfileOrThrowError(profileId);
        profile.editRole(role);
        profileRepository.save(profile);
        return modelMapper.map(profile, ProfileDto.class);
    }

    private Profile findProfileOrThrowError(String profileId) {
        return profileRepository.findById(profileId).orElseThrow(NoSuchElementException::new);
    }

    @Override
    public void run(String... args) throws Exception {
        if (!profileRepository.existsByRolesContaining("ADMINISTRATOR")) {
            String password = BCrypt.hashpw("admin", BCrypt.gensalt());
            String email = EmailEncryptionUtils.encryptAndEncodeUserId("adminemail@mail.com");
            Profile adminProfile = new Profile("admin", email, EducationLevel.OTHER, new HashSet<String>(), new Location(), password, Set.of(Roles.ADMINISTRATOR, Roles.MODERATOR, Roles.USER), "", new Stats(), new HashSet<Activity>(), 0.);
            profileRepository.save(adminProfile);
        }
    }
}
