package telran.accounting.dto;

import lombok.Getter;

import java.util.Set;

@Getter
public class ProfileDto {
    protected String username;
    protected String email;
    protected String educationLevel;
    protected Set<String> communities;
    protected LocationDto location;
    protected String password;
    protected Set<String> roles;
    protected String avatar;
    protected StatsDto stats;
    protected Set<ActivityDto> activities;
    protected Double wallet;
}
