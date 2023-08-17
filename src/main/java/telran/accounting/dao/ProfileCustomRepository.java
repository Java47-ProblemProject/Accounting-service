package telran.accounting.dao;

import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import telran.accounting.model.Profile;

@Repository
@AllArgsConstructor
public class ProfileCustomRepository {
    private final MongoTemplate mongoTemplate;
    public void removeKeyFromActivities(String activityId) {
        Update update = new Update().unset("activities." + activityId);
        mongoTemplate.updateMulti(new Query(), update, Profile.class);
    }
}
