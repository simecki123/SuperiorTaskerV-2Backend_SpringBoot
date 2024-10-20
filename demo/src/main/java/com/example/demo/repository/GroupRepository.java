package com.example.demo.repository;

import com.example.demo.models.dao.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends MongoRepository<Group, String> {
    Optional<Group> findByName(String name);
    Optional<Group> findById(String groupId);

}
