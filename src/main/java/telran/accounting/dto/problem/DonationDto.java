package telran.accounting.dto.problem;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DonationDto {
    protected String userId;
    protected Double amount;
    protected LocalDateTime dateDonated;
}