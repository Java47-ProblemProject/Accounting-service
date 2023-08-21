package telran.accounting.dto.problem;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@ToString
public class ProblemDto {
    protected String id;
    protected String author;
    protected String authorId;
    protected Integer rating;
    protected String title;
    protected Set<String> communityNames;
    protected String details;
    protected LocalDateTime dateCreated;
    protected Double currentAward;
    protected ReactionsDto reactions;
    protected Set<DonationDto> donationHistory;
    protected Set<String> comments;
    protected Set<String> solutions;
    protected Set<String> subscribers;
    protected String type;
}
