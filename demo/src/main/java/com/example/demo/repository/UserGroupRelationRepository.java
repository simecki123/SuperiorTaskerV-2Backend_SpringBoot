package com.example.demo.repository;

import com.example.demo.models.dao.UserGroupRelation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGroupRelationRepository extends MongoRepository<UserGroupRelation, String> {
}
