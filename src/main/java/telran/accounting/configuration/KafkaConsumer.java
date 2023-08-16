package telran.accounting.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import telran.accounting.dao.ProfileRepository;
import telran.accounting.dto.ProfileDto;
import telran.accounting.model.Profile;

import java.util.function.Consumer;

@Getter
@Configuration
@RequiredArgsConstructor
public class KafkaConsumer {
    final ProfileRepository profileRepository;
    final ModelMapper modelMapper;
    @Setter
    ProfileDto profile;

    @Bean
    protected Consumer<ProfileDto> receiveUpdatedProfile() {
        return data -> {
            System.out.println(data);
            Profile profileEntity = modelMapper.map(data, Profile.class);
            profileRepository.save(profileEntity);
        };
    }


}
