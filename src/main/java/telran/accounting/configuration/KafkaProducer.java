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
    @Setter
    private ProfileDto profileToComment;

    @Bean
    public Supplier<ProfileDto> sendAuthenticatedProfileToProblem() {
        return () -> {
            if (profileToProblem != null) {
                System.out.println("sendAuthenticatedProfileToProblem : " + profileToProblem);
                streamBridge.send("sendAuthenticatedProfileToProblem-out-0", profileToProblem);
                ProfileDto sentMessage = profileToProblem;
                profileToProblem = null;
                return sentMessage;
            }
            return null;
        };
    }

    @Bean
    public Supplier<ProfileDto> sendAuthenticatedProfileToComment() {
        return () -> {
            if (profileToComment != null) {
                System.out.println("sendAuthenticatedProfileToComment : " + profileToComment);
                streamBridge.send("sendAuthenticatedProfileToComment-out-0",profileToComment);
                ProfileDto sentMessage = profileToComment;
                profileToComment = null;
                return sentMessage;
            }
            return null;
        };
    }

//    public Supplier<ProfileDto> createSupplier(String destination) {
//        return () -> {
//            if (profile != null) {
//                System.out.println(destination + " : " + profile);
//                streamBridge.send(destination, profile);
//                ProfileDto sentMessage = profile;
//                profile = null;
//                return sentMessage;
//            }
//            return null;
//        };
//    }
}
