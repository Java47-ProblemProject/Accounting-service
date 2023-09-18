package telran.accounting.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;
import java.util.Set;

@Getter
@ToString
public class ProfileDto {
    @Setter
    protected String username;
    protected String email;
    protected String educationLevel;
    protected Set<String> communities;
    protected LocationDto location;
    protected Set<String> roles;
    protected String avatar;
    protected StatsDto stats;
    //protected Map<String, ActivityDto> activities;
    protected Double wallet;
}
