package telran.accounting.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Document(collection = "Profiles")
@Getter
@EqualsAndHashCode(of = "email")
@ToString
@AllArgsConstructor
public class Profile {
    @Setter
    protected String username;
    @Id
    @Setter
    protected String email;
    @Setter
    protected EducationLevel educationLevel;
    @Singular
    protected Set<String> communities;
    @Setter
    protected Location location;
    @Setter
    protected String password;
    @Singular
    @Setter
    protected Set<Roles> roles;
    @Setter
    protected String avatar;
    @Setter
    protected Stats stats;
    @Setter
    protected Map<String, Activity> activities;
    @Setter
    protected Double wallet;

    public Profile() {
        this.roles = new HashSet<>();
        this.communities = new HashSet<>();
        this.activities = new HashMap<>();
        this.avatar = "";
        this.stats = new Stats(0, 0, 0, 0);
        this.wallet = 0.;
    }

    public void addActivity(String id, Activity activity) {
        this.activities.put(id, activity);
    }

    public void removeActivity(String id) {
        this.activities.remove(id);
    }

    public void addFormulatedProblem(){
        this.stats.setFormulatedProblems(this.stats.getFormulatedProblems()+1);
    }

    public void removeFormulatedProblem(){
        this.stats.setFormulatedProblems(this.stats.getFormulatedProblems()-1);
    }


    public void editCommunities(Set<String> newCommunities) {
        this.communities = newCommunities;
    }

    public void editStats(String educationLevel) {
        this.stats.setRating(calculateRating(EducationLevel.valueOf(educationLevel)));
    }

    private int calculateRating(EducationLevel educationLevel) {
        return switch (educationLevel) {
            case PRESCHOOL -> 10;
            case PRIMARY -> 20;
            case SECONDARY -> 30;
            case HIGH_SCHOOL -> 40;
            case ASSOCIATE_DEGREE -> 50;
            case BACHELOR_DEGREE -> 60;
            case MASTER_DEGREE -> 70;
            case DOCTORATE_DEGREE -> 80;
            case PROFESSIONAL_DEGREE -> 90;
            case POSTDOCTORAL_FELLOWSHIP -> 100;
            case HONORARY_DEGREE -> 110;
            case OTHER -> 0;
        };
    }
}
