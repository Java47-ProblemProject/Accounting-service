package telran.accounting.model;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@EqualsAndHashCode(of = "email")
@ToString
@AllArgsConstructor
public class Profile {
    @Setter
    protected String username;
    @Id
    protected String email;
    @Setter
    protected String educationLevel;
    @Singular
    protected Set<String> scientificInterests;
    @Setter
    protected Location location;
    @Setter
    protected String password;
    @Singular
    protected Set<String> roles;
    @Setter
    protected String avatar;
    @Setter
    protected Stats stats;
    @Setter
    protected Set<Activity> activities;
    @Setter
    protected Double wallet;

    public Profile() {
        this.roles = new HashSet<>();
        this.scientificInterests = new HashSet<>();
        this.activities = new HashSet<>();
        this.avatar = "";
        this.stats = new Stats(0, 0, 0, 0);
        this.wallet = 0.;
    }

    public void editRole(String... newRoles) {
        roles.addAll(List.of(newRoles));
    }

    public void editScientificInterests(String... newInterests) {
        scientificInterests.addAll(List.of(newInterests));
    }

    public void editStats(String educationLevel) {
        stats.setRating(calculateRating(educationLevel));
    }

    private int calculateRating(String educationLevel) {
        return switch (educationLevel) {
            case "student" -> 10;
            case "bachelor" -> 20;
            case "master" -> 30;
            default -> 0;
        };
    }
}
