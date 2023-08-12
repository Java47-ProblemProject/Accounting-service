package telran.accounting.dto;

import lombok.Getter;
import lombok.ToString;

import java.util.Set;

@Getter
@ToString
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
