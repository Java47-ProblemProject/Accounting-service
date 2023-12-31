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
        this.roles.add(Roles.USER);
        this.communities = new HashSet<>();
        this.avatar = "";
        this.stats = new Stats(0, 0, 0, 0.);
        this.wallet = 0.;
        this.activities = new HashMap<>();
        this.educationLevel = EducationLevel.OTHER;
    }

    public void addActivity(String id, Double problemRating, String entityType, Set<String> actions) {
        if (!this.activities.containsKey(id)) {
            this.activities.put(id, new Activity(entityType, problemRating));
            if (entityType.equals("PROBLEM") && actions.contains("AUTHOR")) {
                this.stats.setFormulatedProblems(this.stats.getFormulatedProblems() + 1);
            } else if (entityType.equals("SOLUTION") && (actions.contains("LIKE") || actions.contains("DISLIKE"))) {
                if (actions.contains("LIKE")) {
                    this.stats.setFormulatedProblems(this.stats.getCheckedSolutions() + 1);
                } else if (actions.contains("DISLIKE")) {
                    this.stats.setFormulatedProblems(this.stats.getCheckedSolutions() - 1);
                }
            }
        }
        this.activities.get(id).action.addAll(actions);
        calculateRating();
    }

    public void removeActivity(String id, String action) {
        Activity activity = this.activities.get(id);
        activity.getAction().remove(action);
        if (activity.getType().equals("PROBLEM") && action.equals("AUTHOR")) {
            this.stats.setFormulatedProblems(this.stats.getFormulatedProblems() - 1);
        } else if (activity.getType().equals("SOLUTION") && (action.contains("LIKE") || action.contains("DISLIKE"))) {
            this.stats.setCheckedSolutions(this.stats.getCheckedSolutions() - 1);
        }
        if (activity.getAction().isEmpty()) {
            this.activities.remove(id);
        }
        calculateRating();
    }

    public void editCommunities(Set<String> newCommunities) {
        this.communities = newCommunities;
    }

    public void calculateRating() {
        double solvedProblems = 0.5 * this.stats.getSolvedProblems();
        double checkedSolutions = 0.3 * this.stats.getCheckedSolutions();
        double formulatedProblemsWeight = this.getActivities().values().stream()
                .filter(a -> a.getType().equals("PROBLEM") && a.getAction().contains("AUTHOR"))
                .mapToDouble(Activity::getRating)
                .sum();
        double formulatedProblems = 0.1 * this.stats.getFormulatedProblems();
        double rating = solvedProblems + checkedSolutions + formulatedProblemsWeight + formulatedProblems;
        this.stats.setRating(Double.parseDouble(String.format("%.2f", switch (this.educationLevel) {
            case PRESCHOOL -> 10 + rating;
            case PRIMARY -> 12 + rating;
            case SECONDARY -> 14 + rating;
            case HIGH_SCHOOL -> 16 + rating;
            case ASSOCIATE_DEGREE -> 18 + rating;
            case BACHELOR_DEGREE -> 20 + rating;
            case MASTER_DEGREE -> 22 + rating;
            case DOCTORATE_DEGREE -> 24 + rating;
            case PROFESSIONAL_DEGREE -> 26 + rating;
            case POSTDOCTORAL_FELLOWSHIP -> 28 + rating;
            case HONORARY_DEGREE -> 30 + rating;
            case OTHER -> rating;
        })));
    }
}
