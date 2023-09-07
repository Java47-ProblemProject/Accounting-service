package telran.accounting.dao;

import org.springframework.data.mongodb.repository.MongoRepository;
import telran.accounting.model.Profile;

import java.util.Set;
import java.util.stream.Stream;

public interface ProfileRepository extends MongoRepository<Profile, String> {
    boolean existsByRolesContaining(String role);
    Stream<Profile> findAllByCommunitiesContaining(Set<String> communities);

    Stream<Profile> findAllByOrderByStats_RatingDesc();
}
