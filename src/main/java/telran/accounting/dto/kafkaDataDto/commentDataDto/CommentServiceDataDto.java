package telran.accounting.dto.kafkaDataDto.commentDataDto;

import lombok.Getter;

@Getter
public class CommentServiceDataDto {
    private String profileId;
    private String problemId;
    private String commentsId;
    private CommentMethodName methodName;
}
