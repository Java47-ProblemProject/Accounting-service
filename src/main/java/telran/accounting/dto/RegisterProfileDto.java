package telran.accounting.dto;

import lombok.Getter;

import java.util.Set;
@Getter
public class RegisterProfileDto {
    protected String username;
    protected String email;
    protected String educationLevel;
    protected Set<String> scientificInterests;
    protected LocationDto location;
    protected String password;
}
