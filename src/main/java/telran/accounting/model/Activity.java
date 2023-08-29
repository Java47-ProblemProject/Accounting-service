package telran.accounting.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Activity {
    protected String type;
    protected Double rating;
    protected Boolean liked;
    protected Boolean disliked;
}
