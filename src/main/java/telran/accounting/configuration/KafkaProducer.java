package telran.accounting.configuration;

import lombok.RequiredArgsConstructor;

import lombok.Setter;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import telran.accounting.dto.ProfileDto;

import java.util.function.Supplier;

@Configuration
@RequiredArgsConstructor
public class KafkaProducer {
    private final StreamBridge streamBridge;
    @Setter
    private ProfileDto profileToProblem;
//    @Setter
//    private ProfileDto profileToComment;

    @Bean
    public Supplier<ProfileDto> sendProfileToProblem() {
        return () -> {
            if (profileToProblem != null) {
                streamBridge.send("sendAuthenticatedProfileToProblem-out-0", profileToProblem);
                ProfileDto sentMessage = profileToProblem;
                profileToProblem = null;
                return sentMessage;
            }
            return null;
        };
    }
}
