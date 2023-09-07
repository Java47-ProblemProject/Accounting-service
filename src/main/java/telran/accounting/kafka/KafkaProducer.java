package telran.accounting.kafka;

import lombok.RequiredArgsConstructor;

import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import telran.accounting.dto.ProfileDto;

import java.util.function.Supplier;

@Configuration
@RequiredArgsConstructor
public class KafkaProducer {
    @Setter
    private ProfileDto profile;

    @Bean
    public Supplier<ProfileDto> sendProfile() {
        return () -> {
            if (profile != null) {
                ProfileDto sentMessage = profile;
                profile = null;
                return sentMessage;
            }
            return null;
        };
    }
}
