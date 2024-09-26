package com.example.demo.repository;

import com.example.demo.models.dao.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.w3c.dom.events.Event;

import java.util.Optional;

@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {
    Optional<Project> getById(String id);
    boolean existsByUserIdGroupIdAndCompletion(String userId, String groupId, double completion);
    Optional<Event> findByUserIdGroupIdAndCompletion(String userId, String groupId, double completion);
}
