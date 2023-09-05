package telran.accounting.dao;

import com.mongodb.BasicDBObject;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import telran.accounting.model.Profile;

import java.util.Set;

@Repository
@AllArgsConstructor
public class ProfileCustomRepository {
    private final MongoTemplate mongoTemplate;

    @Transactional
    public void removeKeysFromActivity(Set<String> activityIds) {
        if (!activityIds.isEmpty()) {
            Query query = new Query();
            Update update = new Update();
            activityIds.forEach(e -> update.unset("activities." + e));
            mongoTemplate.updateMulti(query, update, Profile.class);
        }
    }
}
