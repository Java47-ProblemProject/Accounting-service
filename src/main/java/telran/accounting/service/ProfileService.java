package telran.accounting.service;

import telran.accounting.dto.*;
import telran.accounting.model.EducationLevel;
import telran.accounting.model.Roles;

import java.util.Set;

public interface ProfileService {
    ProfileDto addProfile(RegisterProfileDto newProfile);

    ProfileDto getProfile(String profileId); //The following method works with login(Principal.getStringId) and getProfile(StringId) methods.

    ProfileDto editName(String profileId, NameDto newName);

    ProfileDto editEducation(String profileId, String newEducation);

    ProfileDto editCommunities(String profileId, CommunitiesDto newCommunities);

    ProfileDto editLocation(String profileId, LocationDto newLocation);

    ProfileDto editAvatar(String profileId, String newAvatar);

    Boolean editPassword(String profileId, String newPassword);

    ProfileDto deleteUser(String profileId);

    //Administrative methods//
    ProfileDto editRole(String profileId,String targetId, RoleDto roles);
    ProfileDto deleteUser(String profileId, String targetId);
    ProfileDto deleteAvatar(String profileId, String targetId);

}
