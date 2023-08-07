package telran.accounting.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import telran.accounting.dto.LocationDto;
import telran.accounting.dto.ProfileDto;
import telran.accounting.dto.RegisterProfileDto;
import telran.accounting.service.ProfileService;

import java.security.Principal;
import java.util.Set;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class ProfileController {
    final ProfileService profileService;
    @PostMapping("/registration")
    public ProfileDto addProfile(@RequestBody RegisterProfileDto newProfile) {
        return profileService.addProfile(newProfile);
    }

    public RegisterProfileDto login(Principal principal) {
        return profileService.getProfile(principal.getName());
    }

    public RegisterProfileDto getProfile(String profileId) {
        return profileService.getProfile(profileId);
    }

    public RegisterProfileDto editName(String profileId, String newName) {
        return profileService.editName(profileId, newName);
    }

    public RegisterProfileDto editEducation(String profileId, String newEducation) {
        return profileService.editEducation(profileId, newEducation);
    }

    public RegisterProfileDto editInterests(String profileId, Set<String> newInterests) {
        return profileService.editInterests(profileId, newInterests);
    }

    public RegisterProfileDto editLocation(String profileId, LocationDto newLocation) {
        return profileService.editLocation(profileId, newLocation);
    }

    public RegisterProfileDto editAvatar(String profileId, String newAvatar) {
        return profileService.editAvatar(profileId, newAvatar);
    }

    public Boolean editPassword(String profileId, String newPassword) {
        return profileService.editPassword(profileId, newPassword);
    }

    public RegisterProfileDto deleteUser(String profileId) {
        return profileService.deleteUser(profileId);
    }
}
