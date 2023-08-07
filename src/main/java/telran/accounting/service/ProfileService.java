package telran.accounting.service;

import telran.accounting.dto.LocationDto;
import telran.accounting.dto.ProfileDto;
import telran.accounting.dto.RegisterProfileDto;

import java.util.Set;

public interface ProfileService {
    ProfileDto addProfile(RegisterProfileDto newProfile);
    RegisterProfileDto getProfile(String profileId); //The following method works with login(Principal.getStringId) and getProfile(StringId) methods.
    RegisterProfileDto editName(String profileId, String newName);
    RegisterProfileDto editEducation(String profileId, String newEducation);
    RegisterProfileDto editInterests(String profileId, Set<String> newInterests);
    RegisterProfileDto editLocation(String profileId, LocationDto newLocation);
    RegisterProfileDto editAvatar(String profileId, String newAvatar);
    Boolean editPassword(String profileId, String newPassword);
    RegisterProfileDto deleteUser(String profileId);
}
