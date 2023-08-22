package telran.accounting.configuration;

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
    @Setter
    private String newAuthor;
    @Setter
    private String removedAuthor;

    @Bean//repaired
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

    @Bean
    public Supplier<String> sendNameToChange() {
        return () -> {
            if (newAuthor != null) {
                String sentMessage = newAuthor;
                newAuthor = null;
                return sentMessage;
            }
            return null;
        };
    }

    @Bean
    public Supplier<String> sendAuthorToRemove() {
        return () -> {
            if (removedAuthor != null) {
                String sentMessage = removedAuthor;
                removedAuthor = null;
                return sentMessage;
            }
            return null;
        };
    }
}
