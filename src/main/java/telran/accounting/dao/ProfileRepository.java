package telran.accounting.dao;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import telran.accounting.dto.ActivityDto;
import telran.accounting.dto.ProfileDto;
import telran.accounting.model.Profile;

import java.util.List;
import java.util.Set;

public interface ProfileRepository extends MongoRepository<Profile, String> {
    boolean existsByRolesContaining(String role);

}
