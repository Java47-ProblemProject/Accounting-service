package telran.accounting.service;

import org.springframework.http.ResponseEntity;
import telran.accounting.dto.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProfileService {
    Map<String, String> addProfile(RegisterProfileDto newProfile);

    Map<String, String> logInProfile(String profileId);

    Boolean logOutProfile();

    ProfileDto getProfile(String profileId);
    Set<ProfileDto> getProfilesByCommunities(Set<String> communities);

    Set<ProfileDto> getProfiles();

    ResponseEntity<String> getEmail(String profileId);

    ProfileDto editName(String profileId, NameDto newName);

    ProfileDto editEducation(String profileId, String newEducation);

    ProfileDto editCommunities(String profileId, CommunitiesDto newCommunities);

    ProfileDto editLocation(String profileId, LocationDto newLocation);

    ProfileDto editAvatar(String profileId, String newAvatar);

    Boolean editPassword(String profileId, String newPassword);

    Boolean resetPassword(String emailAddress);

    ProfileDto deleteUser(String profileId);

    //Administrative methods//
    ProfileDto editRole(String profileId, String targetId, RoleDto roles);

    ProfileDto deleteUser(String profileId, String targetId);

    ProfileDto deleteAvatar(String profileId, String targetId);

    List<String> getEducationList();

}
