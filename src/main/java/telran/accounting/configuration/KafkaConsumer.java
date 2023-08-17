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
    @Setter
    String problemIdToDelete;

    @Bean
    protected Consumer<ProfileDto> receiveUpdatedProfile() {
        return data -> {
            Profile profileEntity = modelMapper.map(data, Profile.class);
            profileRepository.save(profileEntity);
        };
    }

    @Bean
    protected Consumer<String> receiveProblemIdToDelete() {
        return data -> {
            profileRepository.findAll().forEach(
                    person -> {
                        person.getActivities().remove(data);
                        profileRepository.save(person);
                    }
            );
        };
    }


}
