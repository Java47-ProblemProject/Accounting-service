package telran.accounting.configuration;

import lombok.RequiredArgsConstructor;

import lombok.Setter;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import telran.accounting.dto.ProfileDto;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class KafkaProducer {
    private final StreamBridge streamBridge;
    @Setter
    private ProfileDto message;

    @Bean
    public Supplier<ProfileDto> sendAuthenticatedProfile() {
        return () -> {
            if (message != null) {
                //System.out.println(message);
                streamBridge.send("sendAuthenticatedProfile-out-0", message);
                ProfileDto sentMessage = message;
                message = null;
                return sentMessage;
            }
            return null;
        };
    }
}
