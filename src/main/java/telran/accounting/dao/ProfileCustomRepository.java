package telran.accounting.dao;

import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import telran.accounting.model.Profile;

import java.util.Set;

@Repository
@AllArgsConstructor
public class ProfileCustomRepository {
    private final MongoTemplate mongoTemplate;
    public void removeKeyFromActivities(Set<String> activityIds) {
        Query query = new Query();
        Update update = new Update();
        for (String activityId : activityIds) {
            update.unset("activities." + activityId);
        }
        mongoTemplate.updateMulti(query, update, Profile.class);
    }
}
