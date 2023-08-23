package telran.accounting.dto.kafkaData;

import lombok.Getter;

import java.util.Set;

@Getter
public class ProblemServiceDataDto {
    private String profileId;
    private String problemId;
    private String methodName;
    private Set<String> comments;
    private Set<String> solutions;
    private Set<String> subscribers;
}
