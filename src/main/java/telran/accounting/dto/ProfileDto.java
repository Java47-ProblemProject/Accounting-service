package telran.accounting.dto;

import lombok.Getter;
import lombok.ToString;

import java.util.Map;
import java.util.Set;

@Getter
@ToString
public class ProfileDto {
    protected String username;
    protected String email; //check if it necessary
    protected String educationLevel;
    protected Set<String> communities;
    protected LocationDto location;
    protected String password; //check if it necessary
    protected Set<String> roles;
    protected String avatar;
    protected StatsDto stats;
    protected Map<String, ActivityDto> activities; //check if it necessary
    protected Double wallet;
}
