package telran.accounting.dto.kafkaDataDto.solutionDataDto;

import lombok.Getter;

@Getter
public class SolutionServiceDataDto {
    private String profileId;
    private String problemId;
    private Double problemRating;
    private String solutionId;
    private SolutionMethodName methodName;
}
