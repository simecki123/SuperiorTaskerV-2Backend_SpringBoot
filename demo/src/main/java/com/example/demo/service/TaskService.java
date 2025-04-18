package com.example.demo.service;

import com.example.demo.models.dto.*;
import com.example.demo.models.enums.TaskStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskService {
    TaskResponse createTask( TaskRequest taskRequest);
    List<TaskResponse> getAllTasksForUser(String userId, String groupId, String projectId, TaskStatus taskStatus, String search, Pageable pageable );
    String updateTaskStatus(String id, TaskStatus taskStatus);
    TaskResponse updateTask(TaskRequest taskRequest, String taskId);
    DeleteResponse deleteTaskById(String taskId);
    List<UserProjectResponse> fetchUserProjectRelations(List<UserProjectRelationRequest> userProjectRelationRequests);




}
