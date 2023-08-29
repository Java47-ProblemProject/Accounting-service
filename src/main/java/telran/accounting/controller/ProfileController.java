package telran.accounting.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import telran.accounting.dto.*;
import telran.accounting.model.EducationLevel;
import telran.accounting.service.ProfileService;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class ProfileController {
    final ProfileService profileService;

    @PostMapping("/registration")
    public ProfileDto addProfile(@RequestBody RegisterProfileDto newProfile) {
        return profileService.addProfile(newProfile);
    }

    @PostMapping("/login")
    public Map<String, ProfileDto> login(Principal principal) {
        return profileService.logInProfile(principal.getName());
    }

    @GetMapping("/getuser/{profileId}")
    public ProfileDto getProfile(@PathVariable String profileId) {
        return profileService.getProfile(profileId);
    }

    @GetMapping("/getcomunityusers")
    public Set<ProfileDto> getProfilesByCommunities(@RequestBody Set<String> communities) {
        return profileService.getProfilesByCommunities(communities);
    }

    @GetMapping("/getallusers")
    public Set<ProfileDto> getProfiles() {
        return profileService.getProfiles();
    }

    @PutMapping("/editname/{profileId}")
    public ProfileDto editName(@PathVariable String profileId, @RequestBody NameDto newName) {
        return profileService.editName(profileId, newName);
    }

    @PutMapping("/editeducation/{profileId}/{newEducation}")
    public ProfileDto editEducation(@PathVariable String profileId, @PathVariable String newEducation) {
        return profileService.editEducation(profileId, newEducation);
    }

    @PutMapping("/editscientificinterests/{profileId}")
    public ProfileDto editCommunities(@PathVariable String profileId, @RequestBody CommunitiesDto newInterests) {
        return profileService.editCommunities(profileId, newInterests);
    }

    @PutMapping("/editlocation/{profileId}")
    public ProfileDto editLocation(@PathVariable String profileId, @RequestBody LocationDto newLocation) {
        return profileService.editLocation(profileId, newLocation);
    }

    @PutMapping("/editavatar/{profileId}/{newAvatar}")
    public ProfileDto editAvatar(@PathVariable String profileId, @PathVariable String newAvatar) {
        return profileService.editAvatar(profileId, newAvatar);
    }

    @PutMapping("/editpassword/{profileId}")
    public Boolean editPassword(@PathVariable String profileId, @RequestHeader("X-Password") String newPassword) {
        return profileService.editPassword(profileId, newPassword);
    }

    @PutMapping("/resetpassword/{emailAddress}")
    public Boolean resetPassword(@PathVariable String emailAddress) {
        return profileService.resetPassword(emailAddress);
    }

    @DeleteMapping("/delete/{profileId}")
    public ProfileDto deleteUser(@PathVariable String profileId) {
        return profileService.deleteUser(profileId);
    }

    @PutMapping("/editrole/{profileId}/{targetId}")
    public ProfileDto editRole(@PathVariable String profileId, @PathVariable String targetId, @RequestBody RoleDto roles) {
        return profileService.editRole(profileId, targetId, roles);
    }

    @DeleteMapping("/deleteuser/{profileId}/{targetId}")
    public ProfileDto deleteUser(@PathVariable String profileId, @PathVariable String targetId) {
        return profileService.deleteUser(profileId, targetId);
    }

    @DeleteMapping("/deleteavatar/{profileId}/{targetId}")
    public ProfileDto deleteAvatar(@PathVariable String profileId, @PathVariable String targetId) {
        return profileService.deleteAvatar(profileId, targetId);
    }

    @GetMapping("/geteducation")
    public List<String> getEducationList() {
        return profileService.getEducationList();
    }
}
