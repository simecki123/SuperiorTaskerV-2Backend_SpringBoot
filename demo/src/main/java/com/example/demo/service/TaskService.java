package com.example.demo.service;

import com.example.demo.models.dto.TaskRequest;
import com.example.demo.models.dto.TaskResponse;
import com.example.demo.models.enums.TaskStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskService {
    TaskResponse createTask( TaskRequest taskRequest);
    TaskResponse getTaskById(String taskId);
    List<TaskResponse> getAllTasksForGroup(String groupId, Pageable pageable);
    List<TaskResponse> getAllTasksForUser(String groupId, String userId, Pageable pageable, TaskStatus taskStatus, String search);
    List<TaskResponse> getAllUserTasks(String userId, Pageable pageable );

    String updateTaskStatus(String id, TaskStatus taskStatus);
    List<TaskResponse> getActiveTasksByGroupId(String groupId, Pageable pageable);
    List<TaskResponse> getActiveTasksByGroupIdAndUserId(String groupId, String userId, Pageable pageable);


}
