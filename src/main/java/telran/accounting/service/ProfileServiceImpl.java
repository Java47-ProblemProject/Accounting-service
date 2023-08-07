package telran.accounting.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import telran.accounting.dao.ProfileRepository;
import telran.accounting.dto.LocationDto;
import telran.accounting.dto.ProfileDto;
import telran.accounting.dto.RegisterProfileDto;
import telran.accounting.model.Profile;
import telran.accounting.model.exceptions.ProfileExistsException;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    final ProfileRepository profileRepository;
    final ModelMapper modelMapper;
    final PasswordEncoder passwordEncoder;

    @Override
    public ProfileDto addProfile(RegisterProfileDto newProfile) {
        if (profileRepository.existsById(newProfile.getEmail())) {
            throw new ProfileExistsException();
        }
        Profile profile = modelMapper.map(newProfile, Profile.class);
        System.out.println(profile);
        String hashedPassword = passwordEncoder.encode(profile.getPassword());
        profile.setPassword(hashedPassword);
        profile.editStats(profile.getEducationLevel());
        profile.editRole("USER");
        profileRepository.save(profile);
        return modelMapper.map(profile, ProfileDto.class);
    }

    @Override
    public RegisterProfileDto getProfile(String profileId) {
        return null;
    }

    @Override
    public RegisterProfileDto editName(String profileId, String newName) {
        return null;
    }

    @Override
    public RegisterProfileDto editEducation(String profileId, String newEducation) {
        return null;
    }

    @Override
    public RegisterProfileDto editInterests(String profileId, Set<String> newInterests) {
        return null;
    }

    @Override
    public RegisterProfileDto editLocation(String profileId, LocationDto newLocation) {
        return null;
    }

    @Override
    public RegisterProfileDto editAvatar(String profileId, String newAvatar) {
        return null;
    }

    @Override
    public Boolean editPassword(String profileId, String newPassword) {
        return null;
    }

    @Override
    public RegisterProfileDto deleteUser(String profileId) {
        return null;
    }
}
