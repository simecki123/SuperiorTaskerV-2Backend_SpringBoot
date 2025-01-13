package com.example.demo.repository;

import com.example.demo.models.dao.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
    Optional<Task> getById(String id);
    boolean existsByUserIdAndProjectId(String userId, String projectId);
    List<Task> findAllByProjectId(String projectId);
    List<Task> findAllByGroupId(String taskId);
    List<Task> findAllByUserId(String userId);
    Task findFirstByUserIdAndProjectIdAndGroupId(String userId, String projectId, String groupId);
    Task findFirstByUserIdAndProjectId(String userId, String projectId);
}
