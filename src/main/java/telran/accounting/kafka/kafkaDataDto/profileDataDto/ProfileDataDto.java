package telran.accounting.kafka.kafkaDataDto.profileDataDto;

import lombok.Getter;
import telran.accounting.model.Activity;

import java.util.Map;
import java.util.Set;

@Getter
public class ProfileDataDto {
    private String token;
    private final String userName;
    private final String email;
    private final Double rating;
    private final Set<String> communities;
    private final Map<String, Activity> activities;
    private final ProfileMethodName methodName;

    public ProfileDataDto(String token, String userName, String email, Double rating, Set<String> communities, Map<String, Activity> activities, ProfileMethodName methodName) {
        this.token = token;
        this.userName = userName;
        this.email = email;
        this.rating = rating;
        this.communities = communities;
        this.activities = activities;
        this.methodName = methodName;
    }

    public ProfileDataDto(String userName, String email, Double rating, Set<String> communities, Map<String, Activity> activities, ProfileMethodName methodName) {
        this.userName = userName;
        this.email = email;
        this.rating = rating;
        this.communities = communities;
        this.activities = activities;
        this.methodName = methodName;
    }
}
