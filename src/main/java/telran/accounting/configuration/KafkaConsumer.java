package telran.accounting.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import telran.accounting.dao.ProfileCustomRepository;
import telran.accounting.dao.ProfileRepository;
import telran.accounting.dto.ProfileDto;
import telran.accounting.dto.problem.ProblemDto;
import telran.accounting.model.Profile;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Getter
@Configuration
@RequiredArgsConstructor
public class KafkaConsumer {
    final ProfileRepository profileRepository;
    final ProfileCustomRepository profileCustomRepository;
    final ModelMapper modelMapper;

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
    protected Consumer<ProblemDto> receiveProblemIdToDelete() {
        return data -> {
            Set<String> itemsToDelete = new HashSet<>();
            itemsToDelete.addAll(data.getComments());
            itemsToDelete.addAll(data.getSolutions());
            itemsToDelete.add(data.getId());
            profileCustomRepository.removeKeyFromActivities(itemsToDelete);
        };
    }

    @Bean
    @Transactional
    protected Consumer<String> receiveCommentIdToDelete() {
        return data -> {
            String commentId = data.split(",")[1];
            profileCustomRepository.removeKeyFromActivities(Set.of(commentId));
        };
    }
}
