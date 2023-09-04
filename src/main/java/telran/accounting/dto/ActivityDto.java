package telran.accounting.dto;

import lombok.Getter;

import java.util.Set;

@Getter
public class ActivityDto {
    protected String type;
    protected String problemId;
    protected Double rating;
    protected Set<String> action;
}
