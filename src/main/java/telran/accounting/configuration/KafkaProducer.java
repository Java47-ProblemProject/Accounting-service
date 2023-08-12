package telran.accounting.configuration;

import lombok.RequiredArgsConstructor;

import lombok.Setter;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class KafkaProducer {
    private final StreamBridge streamBridge;
    @Setter
    private String message;

    @Bean
    public Supplier<String> sendAuthenticatedProfile() {
        return () -> {
            if (message != null) {
                //System.out.println(message);
                streamBridge.send("sendAuthenticatedProfile-out-0", message);
                String sentMessage = message;
                message = null;
                return sentMessage;
            }
            return null;
        };
    }
}
