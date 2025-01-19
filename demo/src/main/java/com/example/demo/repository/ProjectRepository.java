package com.example.demo.repository;

import com.example.demo.models.dao.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {
    Optional<Project> findById(String id);
    boolean existsByUserIdAndGroupIdAndCompletion(String userId, String groupId, double completion);
    Optional<Project> findByUserIdAndGroupIdAndCompletion(String userId, String groupId, double completion);
    List<Project> findAllByGroupIdAndCompletion(String groupId, String completion);
    List<Project> findAllByUserId(String userId);
    List<Project> findAllByGroupId(String groupId);
}