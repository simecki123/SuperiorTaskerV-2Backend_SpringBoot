package com.example.demo.repository;

import com.example.demo.models.dao.UserGroupRelation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserGroupRelationRepository extends MongoRepository<UserGroupRelation, String> {
    List<UserGroupRelation> findAllByUserId(String userId);
    List<UserGroupRelation> findAllByGroupId(String groupId);
    UserGroupRelation findByUserIdAndGroupId(String userId, String groupId);
}
