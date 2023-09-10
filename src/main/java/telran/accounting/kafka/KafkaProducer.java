package telran.accounting.kafka;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import telran.accounting.kafka.kafkaDataDto.profileDataDto.ProfileDataDto;

import java.util.function.Supplier;

@Configuration
@RequiredArgsConstructor
public class KafkaProducer {
    @Setter
    private ProfileDataDto profile;

    @Bean
    public Supplier<ProfileDataDto> sendProfile() {
        return () -> {
            if (profile != null) {
                ProfileDataDto sentMessage = profile;
                this.profile = null;
                return sentMessage;
            }
            return null;
        };
    }
}
