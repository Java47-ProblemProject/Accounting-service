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
import telran.accounting.dto.kafkaData.CommentServiceDataDto;
import telran.accounting.dto.kafkaData.ProblemServiceDataDto;
import telran.accounting.model.Activity;
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
    final KafkaProducer kafkaProducer;

    @Bean
    @Transactional
    protected Consumer<ProblemServiceDataDto> receiveDataFromProblem() {
        return data -> {
            String profileId = data.getProfileId();
            String problemId = data.getProblemId();
            String methodName = data.getMethodName();
            Set<String> comments = data.getComments();
            Set<String> solutions = data.getSolutions();
            Set<String> subscribers = data.getSubscribers();
            if (methodName.equals("addProblem")) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(problemId, new Activity("PROBLEM", false, false));
                profile.addFormulatedProblem();
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals("deleteProblem")) {
                Set<String> problemIdWithCommentsSolutions = new HashSet<>();
                problemIdWithCommentsSolutions.add(problemId);
                problemIdWithCommentsSolutions.addAll(comments);
                problemIdWithCommentsSolutions.addAll(solutions);

                profileCustomRepository.removeKeyFromActivities(problemIdWithCommentsSolutions);

                Profile profile = profileRepository.findById(profileId).get();
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals("addLike")) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(problemId, new Activity("PROBLEM", true, false));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals("removeLike")) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(problemId, new Activity("PROBLEM", false, false));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals("addDislike")) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(problemId, new Activity("PROBLEM", false, true));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals("removeDislike")) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(problemId, new Activity("PROBLEM", false, false));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals("removeLikeRemoveActivity") || methodName.equals("removeDislikeRemoveActivity")) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.removeActivity(problemId);
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals("subscribe")) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(problemId, new Activity("PROBLEM", false, false));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals("unsubscribe")) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.removeActivity(problemId);
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals("donate")) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(problemId, new Activity("PROBLEM", false, false));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
        };
    }

    @Bean
    @Transactional
    protected Consumer<CommentServiceDataDto> receiveDataFromComment() {
        return data -> {
            String profileId = data.getProfileId();
            String problemId = data.getProblemId();
            String methodName = data.getMethodName();
            String commentId = data.getCommentsId();
            if (methodName.equals("addComment")) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(commentId, new Activity("COMMENT", false, false));
                if (!profile.getActivities().containsKey(problemId)) {
                    profile.addActivity(problemId, new Activity("PROBLEM", false, false));
                }
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals("addLike")) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(commentId, new Activity("COMMENT", true, false));
                if (!profile.getActivities().containsKey(problemId)) {
                    profile.addActivity(problemId, new Activity("PROBLEM", false, false));
                }
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals("removeLike")) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(problemId, new Activity("COMMENT", false, false));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals("addDislike")) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(commentId, new Activity("COMMENT", false, true));
                if (!profile.getActivities().containsKey(problemId)) {
                    profile.addActivity(problemId, new Activity("PROBLEM", false, false));
                }
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals("removeDislike")) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(commentId, new Activity("COMMENT", false, false));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals("removeLikeRemoveActivities") || methodName.equals("removeDislikeRemoveActivities")) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.removeActivity(commentId);
                profile.removeActivity(problemId);
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals("deleteComment")) {
                Set<String> commentIdToDelete = new HashSet<>(Set.of(commentId));

                profileCustomRepository.removeKeyFromActivities(commentIdToDelete);

                Profile profile = profileRepository.findById(profileId).get();
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals("deleteCommentAndProblem")) {
                Set<String> commentIdWithProblemId = new HashSet<>();
                commentIdWithProblemId.add(problemId);
                commentIdWithProblemId.add(commentId);

                profileCustomRepository.removeKeyFromActivities(commentIdWithProblemId);

                Profile profile = profileRepository.findById(profileId).get();
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }


        };
    }


}
