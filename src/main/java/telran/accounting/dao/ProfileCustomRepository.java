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
        Query query = new Query();
        Update update = new Update();
        activityIds.forEach(activityId -> update.unset("activities." + activityId));
        mongoTemplate.updateMulti(query, update, Profile.class);
    }

    @Transactional
    public void removeActivitiesByProblemIds(Set<String> problemIdsToRemove) {
        Query query = new Query(Criteria.where("activities").exists(true));
        mongoTemplate.find(query, Profile.class).forEach(profile -> {
            profile.getActivities().entrySet().removeIf(entry -> problemIdsToRemove.contains(entry.getValue().getProblemId()));
            mongoTemplate.save(profile);
        });
    }
}
