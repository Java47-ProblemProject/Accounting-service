package telran.accounting.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class Activity {
    protected String type;
    @Setter
    protected String problemId;
    @Setter
    protected Double rating;
    protected Set<String> action;

    public Activity(String type, Double rating) {
        this.type = type;
        this.rating = rating;
        this.action = new HashSet<>();
        this.problemId = "";
    }
}
