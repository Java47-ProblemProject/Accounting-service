package telran.accounting.kafka;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import telran.accounting.dto.ProfileDto;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Configuration
@RequiredArgsConstructor
public class KafkaProducer {
    private Map<String, ProfileDto> profileData;

    @Bean
    public Supplier<Map<String, ProfileDto>> sendProfile() {
        return () -> {
            if (profileData != null) {
                Map<String, ProfileDto> sentMessage = profileData;
                this.profileData = null;
                return sentMessage;
            }
            return null;
        };
    }

    public void sendProfileData(String token, ProfileDto profile){
        this.profileData = new HashMap<>();
        this.profileData.put(token, profile);
    }

}
