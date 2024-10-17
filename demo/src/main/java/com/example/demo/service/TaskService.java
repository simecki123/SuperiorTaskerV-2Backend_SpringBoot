package com.example.demo.service;

import com.example.demo.models.dto.TaskRequest;
import com.example.demo.models.dto.TaskResponse;
import com.example.demo.models.enums.TaskStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskService {
    TaskResponse createTask( TaskRequest taskRequest);
    TaskResponse getTaskById(String taskId);

    List<TaskResponse> getAllTasksForUser(String userId, String groupId, String projectId, TaskStatus taskStatus, String search, Pageable pageable );

    String updateTaskStatus(String id, TaskStatus taskStatus);

    String deleteTaskById(String taskId);




}
