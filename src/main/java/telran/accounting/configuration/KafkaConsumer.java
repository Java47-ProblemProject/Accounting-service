package telran.accounting.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import telran.accounting.dao.ProfileCustomRepository;
import telran.accounting.dao.ProfileRepository;
import telran.accounting.dto.ProfileDto;
import telran.accounting.dto.problem.ProblemDto;
import telran.accounting.model.Activity;
import telran.accounting.model.Profile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Getter
@Configuration
@RequiredArgsConstructor
public class KafkaConsumer {
    final ProfileRepository profileRepository;
    final ProfileCustomRepository profileCustomRepository;
    private final TransactionTemplate transactionTemplate;
    final ModelMapper modelMapper;
    final KafkaProducer kafkaProducer;

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
    protected Consumer<String> receiveDataFromProblem() {
        return data -> {
            String[] dataSet = data.split(",");
            String profileId = dataSet[0];
            String problemId = dataSet[1];
            String method = dataSet[2];
            String[] otherData = dataSet[3].split(";");
            if (method.equals("addProblem")) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(problemId, new Activity("PROBLEM", false, false));
                profile.addFormulatedProblem();
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (method.equals("deleteProblem")) {
                Set<String> problemIdWithCommentsSolutions = new HashSet<>(List.of(otherData));
                problemIdWithCommentsSolutions.add(problemId);
                transactionTemplate.execute(status -> {
                    profileCustomRepository.removeKeyFromActivities(problemIdWithCommentsSolutions);
                    return null;
                });
                Profile profile = profileRepository.findById(profileId).get();
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (method.equals("addLike")) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(problemId, new Activity("PROBLEM", true, false));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (method.equals("removeLike")) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(problemId, new Activity("PROBLEM", false, false));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (method.equals("addDislike")) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(problemId, new Activity("PROBLEM", false, true));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (method.equals("removeDislike")) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(problemId, new Activity("PROBLEM", false, false));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (method.equals("removeLikeRemoveActivity") || method.equals("removeDislikeRemoveActivity")) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.removeActivity(problemId);
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
        };
    }
}
