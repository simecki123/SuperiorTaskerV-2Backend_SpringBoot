package com.example.demo.service;

import com.example.demo.models.dto.TaskRequest;
import com.example.demo.models.dto.TaskResponse;
import com.example.demo.models.enums.TaskStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskService {
    TaskResponse createTask(String groupId, TaskRequest taskRequest);
    TaskResponse getTaskById(String taskId);
    List<TaskResponse> getAllTasksForGroup(String groupId);
    List<TaskResponse> getAllTasksForUser(String groupId, String userId, Pageable pageable, TaskStatus taskStatus, String search);

    String updateTaskStatus(String id, TaskStatus taskStatus);
    List<TaskResponse> getActiveTasksByGroupId(String groupId);
    List<TaskResponse> getActiveTasksByGroupIdAndUserId(String groupId, String userId);


}
