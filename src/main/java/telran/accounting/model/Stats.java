package telran.accounting.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Stats {
    protected Integer solvedProblems;
    protected Integer checkedSolutions;
    protected Integer formulatedProblems;
    protected Double rating;
}
