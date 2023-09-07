package telran.accounting.kafka;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import telran.accounting.dao.ProfileCustomRepository;
import telran.accounting.dao.ProfileRepository;
import telran.accounting.dto.ProfileDto;
import telran.accounting.kafka.kafkaDataDto.commentDataDto.CommentMethodName;
import telran.accounting.kafka.kafkaDataDto.commentDataDto.CommentServiceDataDto;
import telran.accounting.kafka.kafkaDataDto.problemDataDto.ProblemMethodName;
import telran.accounting.kafka.kafkaDataDto.problemDataDto.ProblemServiceDataDto;
import telran.accounting.kafka.kafkaDataDto.solutionDataDto.SolutionMethodName;
import telran.accounting.kafka.kafkaDataDto.solutionDataDto.SolutionServiceDataDto;
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
            String problemAuthorId = data.getProblemAuthorId();
            Double problemRating = data.getProblemRating();
            String profileId = data.getAuthorizedProfileId();
            String problemId = data.getProblemId();
            ProblemMethodName methodName = data.getMethodName();
            Set<String> comments = data.getComments();
            Set<String> solutions = data.getSolutions();
            if (methodName.equals(ProblemMethodName.ADD_PROBLEM)) {
                Profile profile = profileRepository.findById(problemAuthorId).get();
                profile.addActivity(problemId, problemRating, "PROBLEM", Set.of("AUTHOR", "SUBSCRIPTION"));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(ProblemMethodName.DELETE_PROBLEM)) {
                Profile profile = removeAllActions(problemAuthorId, problemId);
                Set<String> problemIdWithCommentsSolutions = new HashSet<>();
                problemIdWithCommentsSolutions.add(problemId);
                problemIdWithCommentsSolutions.addAll(comments);
                problemIdWithCommentsSolutions.addAll(solutions);
                profileCustomRepository.removeKeysFromActivity(problemIdWithCommentsSolutions);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(ProblemMethodName.ADD_LIKE)) {
                editAction(profileId, problemId, problemId, problemRating, "PROBLEM", "LIKE");
                Profile profile = profileRepository.findById(problemAuthorId).get();
                profile.getActivities().get(problemId).setRating(problemRating);
                profile.calculateRating();
                profileRepository.save(profile);
            }
            if (methodName.equals(ProblemMethodName.ADD_DISLIKE)) {
                editAction(profileId, problemId, problemId, problemRating, "PROBLEM", "DISLIKE");
                Profile profile = profileRepository.findById(problemAuthorId).get();
                profile.getActivities().get(problemId).setRating(problemRating);
                profile.calculateRating();
                profileRepository.save(profile);
            }
            if (methodName.equals(ProblemMethodName.SUBSCRIBE)) {
                Profile profile = profileRepository.findById(profileId).get();
                if (profile.getActivities().containsKey(problemId) && profile.getActivities().get(problemId).getAction().contains("SUBSCRIPTION")) {
                    profile.removeActivity(problemId, "SUBSCRIPTION");
                    profileRepository.save(profile);
                    kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
                    profile = profileRepository.findById(problemAuthorId).get();
                    profile.getActivities().get(problemId).setRating(problemRating);
                    profile.calculateRating();
                    profileRepository.save(profile);
                } else {
                    profile.addActivity(problemId, problemRating, "PROBLEM", Set.of("SUBSCRIPTION"));
                    profileRepository.save(profile);
                    kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
                    profile = profileRepository.findById(problemAuthorId).get();
                    profile.getActivities().get(problemId).setRating(problemRating);
                    profile.calculateRating();
                    profileRepository.save(profile);
                }
            }
            if (methodName.equals(ProblemMethodName.DONATE)) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(problemId, problemRating, "PROBLEM", Set.of("DONATION", "SUBSCRIPTION"));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
                profile = profileRepository.findById(problemAuthorId).get();
                profile.getActivities().get(problemId).setRating(problemRating);
                profile.calculateRating();
                profileRepository.save(profile);
            }
        };
    }

    @Bean
    @Transactional
    protected Consumer<CommentServiceDataDto> receiveDataFromComment() {
        return data -> {
            String profileId = data.getProfileId();
            String problemId = data.getProblemId();
            Double problemRating = data.getProblemRating();
            CommentMethodName methodName = data.getMethodName();
            String commentId = data.getCommentsId();
            if (methodName.equals(CommentMethodName.ADD_COMMENT)) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(commentId, 0., "COMMENT", Set.of("AUTHOR"));
                profile.getActivities().get(commentId).setProblemId(problemId);
                profile.addActivity(problemId, problemRating, "PROBLEM", Set.of("SUBSCRIPTION"));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(CommentMethodName.ADD_LIKE)) {
                editAction(profileId, commentId, problemId, problemRating, "COMMENT", "LIKE");
            }
            if (methodName.equals(CommentMethodName.ADD_DISLIKE)) {
                editAction(profileId, commentId, problemId, problemRating, "COMMENT", "DISLIKE");
            }
            if (methodName.equals(CommentMethodName.DELETE_COMMENT)) {
                Profile profile = removeAllActions(profileId, problemId);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
                profileCustomRepository.removeKeysFromActivity(Set.of(commentId));
            }
        };
    }

    @Bean
    @Transactional
    protected Consumer<SolutionServiceDataDto> receiveDataFromSolution() {
        return data -> {
            String profileId = data.getProfileId();
            String problemId = data.getProblemId();
            Double problemRating = data.getProblemRating();
            SolutionMethodName methodName = data.getMethodName();
            String solutionId = data.getSolutionId();
            if (methodName.equals(SolutionMethodName.ADD_SOLUTION)) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(solutionId, 0., "SOLUTION", Set.of("AUTHOR"));
                profile.getActivities().get(solutionId).setProblemId(problemId);
                profile.addActivity(problemId, problemRating, "PROBLEM", Set.of("SUBSCRIPTION"));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(SolutionMethodName.ADD_LIKE)) {
                editAction(profileId, solutionId, problemId, problemRating, "SOLUTION", "LIKE");
            }
            if (methodName.equals(SolutionMethodName.ADD_DISLIKE)) {
                editAction(profileId, solutionId, problemId, problemRating, "SOLUTION", "DISLIKE");
            }
            if (methodName.equals(SolutionMethodName.DELETE_SOLUTION)) {
                Profile profile = removeAllActions(profileId, problemId);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
                profileCustomRepository.removeKeysFromActivity(Set.of(solutionId));
            }

        };
    }

    private Profile removeAllActions(String profileId, String entityId) {
        Profile profile = profileRepository.findById(profileId).get();
        profile.removeActivity(entityId, "AUTHOR");
        profile.removeActivity(entityId, "LIKE");
        profile.removeActivity(entityId, "DISLIKE");
        profile.removeActivity(entityId, "SUBSCRIPTION");
        profileRepository.save(profile);
        return profile;
    }

    private void editAction(String profileId, String entityId, String problemId, Double problemRating, String type, String action) {
        Profile profile = profileRepository.findById(profileId).get();
        if (profile.getActivities().containsKey(entityId) && profile.getActivities().get(entityId).getAction().contains(action)) {
            profile.removeActivity(entityId, action);
        } else {
            profile.addActivity(entityId, problemRating, type, Set.of(action));
            profile.addActivity(problemId, problemRating, "PROBLEM", Set.of("SUBSCRIPTION"));
            if (action.equals("LIKE")){
                profile.removeActivity(entityId,  "DISLIKE");
            }
            if (action.equals("DISLIKE")){
                profile.removeActivity(entityId,  "LIKE");
            }
        }
        profileRepository.save(profile);
        System.out.println("profile saved - " + profile.toString());
        kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
    }
}
