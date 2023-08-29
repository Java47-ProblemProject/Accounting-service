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
import telran.accounting.dto.kafkaDataDto.commentDataDto.CommentMethodName;
import telran.accounting.dto.kafkaDataDto.commentDataDto.CommentServiceDataDto;
import telran.accounting.dto.kafkaDataDto.problemDataDto.ProblemMethodName;
import telran.accounting.dto.kafkaDataDto.problemDataDto.ProblemServiceDataDto;
import telran.accounting.dto.kafkaDataDto.solutionDataDto.SolutionMethodName;
import telran.accounting.dto.kafkaDataDto.solutionDataDto.SolutionServiceDataDto;
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
            String problemAuthorId = data.getProblemAuthorId();
            Double problemRating = data.getProblemRating();
            String profileId = data.getAuthorizedProfileId();
            String problemId = data.getProblemId();
            ProblemMethodName methodName = data.getMethodName();
            Set<String> comments = data.getComments();
            Set<String> solutions = data.getSolutions();
            if (methodName.equals(ProblemMethodName.ADD_PROBLEM)) {
                Profile profile = profileRepository.findById(problemAuthorId).get();
                profile.addActivity(problemId, addNewActivity("PROBLEM", problemRating, false, false));
                profile.addFormulatedProblem();
                profile.editStats(String.valueOf(profile.getEducationLevel()));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(ProblemMethodName.DELETE_PROBLEM)) {
                Profile profile = profileRepository.findById(problemAuthorId).get();
                profile.removeActivity(problemId);
                profile.removeFormulatedProblem();
                profile.editStats(String.valueOf(profile.getEducationLevel()));
                profileRepository.save(profile);
                Set<String> problemIdWithCommentsSolutions = new HashSet<>();
                problemIdWithCommentsSolutions.add(problemId);
                problemIdWithCommentsSolutions.addAll(comments);
                problemIdWithCommentsSolutions.addAll(solutions);
                profileCustomRepository.removeKeyFromActivities(problemIdWithCommentsSolutions);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(ProblemMethodName.ADD_LIKE)) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(problemId, addNewActivity("PROBLEM", problemRating, true, false));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(ProblemMethodName.REMOVE_LIKE)) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(problemId, addNewActivity("PROBLEM", problemRating, false, false));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(ProblemMethodName.ADD_DISLIKE)) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(problemId, addNewActivity("PROBLEM", problemRating, false, true));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(ProblemMethodName.REMOVE_DISLIKE)) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(problemId, addNewActivity("PROBLEM", problemRating, false, false));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(ProblemMethodName.REMOVE_LIKE_REMOVE_ACTIVITY) || methodName.equals(ProblemMethodName.REMOVE_DISLIKE_REMOVE_ACTIVITY)) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.removeActivity(problemId);
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(ProblemMethodName.SUBSCRIBE) || methodName.equals(ProblemMethodName.DONATE)) {
                Profile profile = profileRepository.findById(profileId).get();
                if (!profile.getActivities().containsKey(problemId)) {
                    profile.addActivity(problemId, addNewActivity("PROBLEM", problemRating, false, false));
                }
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(ProblemMethodName.UNSUBSCRIBE)) {
                Profile profile = profileRepository.findById(profileId).get();
                if (!profile.getEmail().equals(problemAuthorId) && !profile.getActivities().get(problemId).getLiked() && !profile.getActivities().get(problemId).getDisliked()) {
                    profile.removeActivity(problemId);
                }
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
            Double problemRating = data.getProblemRating();
            CommentMethodName methodName = data.getMethodName();
            String commentId = data.getCommentsId();
            if (methodName.equals(CommentMethodName.ADD_COMMENT)) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(commentId, addNewActivity("COMMENT", 0., false, false));
                if (!profile.getActivities().containsKey(problemId)) {
                    profile.addActivity(problemId, addNewActivity("PROBLEM", problemRating, false, false));
                }
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(CommentMethodName.ADD_LIKE)) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(commentId, addNewActivity("COMMENT", 0., true, false));
                if (!profile.getActivities().containsKey(problemId)) {
                    profile.addActivity(problemId, addNewActivity("PROBLEM", problemRating, false, false));
                }
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(CommentMethodName.REMOVE_LIKE)) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(commentId, addNewActivity("COMMENT", 0., false, false));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(CommentMethodName.ADD_DISLIKE)) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(commentId, addNewActivity("COMMENT", 0., false, true));
                if (!profile.getActivities().containsKey(problemId)) {
                    profile.addActivity(problemId, addNewActivity("PROBLEM", problemRating, false, false));
                }
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(CommentMethodName.REMOVE_DISLIKE)) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(commentId, addNewActivity("COMMENT", 0., false, false));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(CommentMethodName.REMOVE_LIKE_REMOVE_COMMENT_ACTIVITY) || methodName.equals(CommentMethodName.REMOVE_DISLIKE_REMOVE_COMMENT_ACTIVITY)) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.removeActivity(commentId);
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(CommentMethodName.REMOVE_LIKE_REMOVE_ALL_ACTIVITIES) || methodName.equals(CommentMethodName.REMOVE_DISLIKE_REMOVE_ALL_ACTIVITIES)) {
                Profile profile = profileRepository.findById(profileId).get();
                System.out.println(profile);
                profile.removeActivity(commentId);
                if (!profile.getActivities().get(problemId).getLiked() && !profile.getActivities().get(problemId).getDisliked()) {
                    profile.removeActivity(problemId);
                }
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(CommentMethodName.DELETE_COMMENT)) {
                Set<String> commentIdToDelete = new HashSet<>(Set.of(commentId));
                profileCustomRepository.removeKeyFromActivities(commentIdToDelete);
                Profile profile = profileRepository.findById(profileId).get();
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(CommentMethodName.DELETE_COMMENT_AND_PROBLEM)) {
                Set<String> commentIdWithProblemId = new HashSet<>();
                commentIdWithProblemId.add(problemId);
                commentIdWithProblemId.add(commentId);
                profileCustomRepository.removeKeyFromActivities(commentIdWithProblemId);
                Profile profile = profileRepository.findById(profileId).get();
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
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
                profile.addActivity(solutionId,addNewActivity("SOLUTION", 0., false, false));
                if (!profile.getActivities().containsKey(problemId)) {
                    profile.addActivity(problemId, addNewActivity("PROBLEM", problemRating, false, false));
                }
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(SolutionMethodName.ADD_LIKE)) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(solutionId, addNewActivity("SOLUTION", 0., true, false));
                if (!profile.getActivities().containsKey(problemId)) {
                    profile.addActivity(problemId, addNewActivity("PROBLEM", problemRating, false, false));
                }
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(SolutionMethodName.REMOVE_LIKE)) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(problemId, addNewActivity("SOLUTION", 0., false, false));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(SolutionMethodName.ADD_DISLIKE)) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(solutionId, addNewActivity("SOLUTION", 0., false, true));
                if (!profile.getActivities().containsKey(problemId)) {
                    profile.addActivity(problemId, addNewActivity("PROBLEM", problemRating, false, false));
                }
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(SolutionMethodName.REMOVE_LIKE)) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.addActivity(solutionId, addNewActivity("SOLUTION", 0., false, false));
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(SolutionMethodName.REMOVE_LIKE_REMOVE_COMMENT_ACTIVITY) || methodName.equals(SolutionMethodName.REMOVE_DISLIKE_REMOVE_COMMENT_ACTIVITY)) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.removeActivity(solutionId);
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(SolutionMethodName.REMOVE_LIKE_REMOVE_ALL_ACTIVITIES) || methodName.equals(SolutionMethodName.REMOVE_DISLIKE_REMOVE_ALL_ACTIVITIES)) {
                Profile profile = profileRepository.findById(profileId).get();
                profile.removeActivity(solutionId);
                if (!profile.getActivities().get(problemId).getLiked() && !profile.getActivities().get(problemId).getDisliked()) {
                    profile.removeActivity(problemId);
                }
                profileRepository.save(profile);
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(SolutionMethodName.DELETE_SOLUTION)) {
                Set<String> commentIdToDelete = new HashSet<>(Set.of(solutionId));
                profileCustomRepository.removeKeyFromActivities(commentIdToDelete);
                Profile profile = profileRepository.findById(profileId).get();
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
            if (methodName.equals(SolutionMethodName.DELETE_SOLUTION_AND_PROBLEM)) {
                Set<String> commentIdWithProblemId = new HashSet<>();
                commentIdWithProblemId.add(problemId);
                commentIdWithProblemId.add(solutionId);
                profileCustomRepository.removeKeyFromActivities(commentIdWithProblemId);
                Profile profile = profileRepository.findById(profileId).get();
                kafkaProducer.setProfile(modelMapper.map(profile, ProfileDto.class));
            }
        };
    }
    private Activity addNewActivity(String type, double problemRating, boolean like, boolean dislike) {
        return new Activity(type, problemRating, like, dislike);
    }
}
