package com.example.demo.repository;

import com.example.demo.models.dao.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
    Optional<Task> getById(String id);
    boolean existsByUserIdAndProjectId(String userId, String projectId);
    Optional<Task> findAllByProjectId(String projectId);
}
