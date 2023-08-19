package telran.accounting.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import telran.accounting.dao.ProfileCustomRepository;
import telran.accounting.dao.ProfileRepository;
import telran.accounting.dto.ProfileDto;
import telran.accounting.model.Profile;

import java.util.function.Consumer;

@Getter
@Configuration
@RequiredArgsConstructor
public class KafkaConsumer {
    final ProfileRepository profileRepository;
    final ProfileCustomRepository profileCustomRepository;
    final ModelMapper modelMapper;
    @Setter
    ProfileDto profile;
    @Setter
    String problemIdToDelete;

    @Bean
    @Transactional
    protected Consumer<ProfileDto> receiveUpdatedProfile() {
        return data -> {
            Profile profileEntity = modelMapper.map(data, Profile.class);
            profileRepository.save(profileEntity);
        };
    }

    @Bean
    @Transactional
    protected Consumer<String> receiveProblemIdToDelete() {
        return profileCustomRepository::removeKeyFromActivities;
    }
}
