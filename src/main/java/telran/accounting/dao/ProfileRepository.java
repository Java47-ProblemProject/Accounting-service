package telran.accounting.dao;

import org.springframework.data.mongodb.repository.MongoRepository;
import telran.accounting.model.Profile;

public interface ProfileRepository extends MongoRepository<Profile, String> {
    boolean existsByRolesContaining(String role);
}
