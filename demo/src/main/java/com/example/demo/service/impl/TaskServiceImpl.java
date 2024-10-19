package com.example.demo.service.impl;

import com.example.demo.converters.ConverterService;
import com.example.demo.exceptions.NoGroupFoundException;
import com.example.demo.exceptions.NoProjectFoundException;
import com.example.demo.exceptions.NoTaskFoundException;
import com.example.demo.models.dao.Project;
import com.example.demo.models.dao.Task;
import com.example.demo.models.dto.TaskRequest;
import com.example.demo.models.dto.TaskResponse;
import com.example.demo.models.enums.TaskStatus;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.TaskRepository;
import com.example.demo.service.ProjectService;
import com.example.demo.service.TaskService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final GroupRepository groupRepository;
    private final ConverterService converterService;
    private final ProjectService projectService;
    private final MongoTemplate mongoTemplate;

    @Override
    public TaskResponse createTask(TaskRequest taskRequest) {
        log.info("Starting to create task");
        groupRepository.findById(taskRequest.getGroupId()).orElseThrow(() -> new NoGroupFoundException("No group associated with the groupId"));
        projectRepository.findById(taskRequest.getProjectId()).orElseThrow(()-> new NoProjectFoundException("No project associated with projectId"));


        Task task = new Task();
        task.setUserId(taskRequest.getUserId());
        task.setGroupId(taskRequest.getGroupId());
        task.setProjectId(taskRequest.getProjectId());
        task.setGroupId(taskRequest.getGroupId());
        task.setName(taskRequest.getName());
        task.setDescription(taskRequest.getDescription());
        task.setStartDate(taskRequest.getStartDate());
        task.setEndDate(taskRequest.getEndDate());
        task.setStatus(TaskStatus.IN_PROGRESS);

        taskRepository.save(task);
        log.info("Updating completion of the project...");
        projectService.updateProjectCompletion(task.getProjectId());

        log.info("Task created");
        return converterService.convertToUserTaskDto(task);
    }

    @Override
    public TaskResponse getTaskById(String taskId) {
        Task task = taskRepository.getById(taskId).orElseThrow(() -> new NoTaskFoundException("No task found associated with given ID"));
        log.info("Get task by id finished");
        return converterService.convertToUserTaskDto(task);
    }

    @Override
    public List<TaskResponse> getAllTasksForUser(String userId, String groupId, String projectId, TaskStatus taskStatus, String search, Pageable pageable) {
        log.info("Searching for tasks with userId: {}", userId);

        Criteria criteria = new Criteria();
        criteria.and("userId").is(userId);

        // Optionally include groupId, projectId, taskStatus, and search
        if (groupId != null && !groupId.isEmpty()) {
            criteria.and("groupId").is(groupId);
        }
        if (projectId != null && !projectId.isEmpty()) {
            criteria.and("projectId").is(projectId);
        }
        if (taskStatus != null) {
            criteria.and("status").is(taskStatus);
        }
        if (search != null && !search.isEmpty()) {
            criteria.and("name").regex(search, "i");  // Case-insensitive search
        }

        MatchOperation matchOperation = Aggregation.match(criteria);

        ProjectionOperation taskOperation = Aggregation.project()
                .and("id").as("taskId")
                .and("userId").as("userId")
                .and("groupId").as("groupId")
                .and("projectId").as("projectId")
                .and("name").as("name")
                .and("description").as("description")
                .and("status").as("status")
                .and("startDate").as("startDate")
                .and("endDate").as("endDate");

        SortOperation sortOperation = Aggregation.sort(Sort.by(Sort.Direction.DESC, "createdAt"));
        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                taskOperation,
                sortOperation,
                skipOperation,
                limitOperation
        );

        AggregationResults<Task> results = mongoTemplate.aggregate(aggregation, "tasks", Task.class);
        log.info("Found {} results", results.getMappedResults().size());

        return results.getMappedResults()
                .stream()
                .map(converterService::convertToUserTaskDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponse> getAllTasksByProjectId(String projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NoProjectFoundException("There is no project with thatId") );
        log.info("Searching for tasks with specific project id...");

        List<Task> taskList = taskRepository.findAllByProjectId(projectId);
        return taskList.stream()
                .map(converterService::convertToUserTaskDto)
                .collect(Collectors.toList());



    }


    @Override
    public String updateTaskStatus(String id, TaskStatus taskStatus) {
        Task task = taskRepository.getById(id).orElseThrow(() -> new NoTaskFoundException("There is no task with that id, so it cannot be updated!"));
        task.setStatus(taskStatus);
        taskRepository.save(task);
        log.info("Updating completion of the project...");
        projectService.updateProjectCompletion(task.getProjectId());
        return "Task status is successfully updated...";
    }

    @Override
    public String deleteTaskById(String taskId) {
        Task task = taskRepository.getById(taskId).orElseThrow(() -> new NoTaskFoundException("There is no task with that id, so it cannot be updated!"));
        taskRepository.delete(task);
        log.info("Updating completion of the project...");
        projectService.updateProjectCompletion(task.getProjectId());
        return "Task is successfully deleted...";
    }


}
