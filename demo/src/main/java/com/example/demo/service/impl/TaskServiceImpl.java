package com.example.demo.service.impl;

import com.example.demo.models.dto.TaskRequest;
import com.example.demo.models.dto.TaskResponse;
import com.example.demo.models.enums.TaskStatus;
import com.example.demo.service.TaskService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {
    @Override
    public TaskResponse createTask(String groupId, TaskRequest taskRequest) {
        return null;
    }

    @Override
    public TaskResponse getTaskById(String taskId) {
        return null;
    }

    @Override
    public List<TaskResponse> getAllTasksForGroup(String groupId) {
        return null;
    }

    @Override
    public List<TaskResponse> getAllTasksForUser(String groupId, String userId, Pageable pageable, TaskStatus taskStatus, String search) {
        return null;
    }

    @Override
    public String updateTaskStatus(String id, TaskStatus taskStatus) {
        return null;
    }

    @Override
    public List<TaskResponse> getActiveTasksByGroupId(String groupId) {
        return null;
    }

    @Override
    public List<TaskResponse> getActiveTasksByGroupIdAndUserId(String groupId, String userId) {
        return null;
    }
}
