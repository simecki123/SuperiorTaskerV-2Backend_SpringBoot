package com.example.demo.service.impl;

import com.example.demo.converters.ConverterService;
import com.example.demo.exceptions.NoGroupFoundException;
import com.example.demo.exceptions.NoProjectFoundException;
import com.example.demo.exceptions.NoTaskFoundException;
import com.example.demo.models.dao.Group;
import com.example.demo.models.dao.Project;
import com.example.demo.models.dao.Task;
import com.example.demo.models.dao.User;
import com.example.demo.models.dto.TaskRequest;
import com.example.demo.models.dto.TaskResponse;
import com.example.demo.models.enums.TaskStatus;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.utils.Helper;
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
import org.springframework.data.domain.Pageable;


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
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final ConverterService converterService;
    private final ProjectService projectService;
    private final MongoTemplate mongoTemplate;

    @Override
    public TaskResponse createTask(TaskRequest taskRequest) {
        log.info("Starting to create task");
        groupRepository.findById(taskRequest.getGroupId()).orElseThrow(() -> new NoGroupFoundException("No group associated with the groupId"));
        projectRepository.findById(taskRequest.getProjectId()).orElseThrow(()-> new NoProjectFoundException("No project associated with projectId"));


        User user = userRepository.getUserById(Helper.getLoggedInUserId());

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
    public List<TaskResponse> getAllTasksForGroup(String groupId, Pageable pageable) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new NoGroupFoundException("There is no group, so no tasks as well"));
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();

        Criteria criteria = Criteria.where("groupId").is(groupId);
        criteria.and("userId").ne(Helper.getLoggedInUserId());

        MatchOperation matchOperation = Aggregation.match(criteria);

        ProjectionOperation taskOperation = Aggregation.project()
                .and("id").as("taskId")
                .and("userId").as("userId")
                .and ("groupId").as("groupId")
                .and("projectId").as("projectId")
                .and("name").as("name")
                .and("description").as("description")
                .and("startDate").as("startDate")
                .and("endDate").as("endDate");

        SortOperation sortOperation = Aggregation.sort(Sort.by(Sort.Direction.DESC, "createdAt"));
        SkipOperation skipOperation = Aggregation.skip((long) pageNumber * pageSize);
        LimitOperation limitOperation = Aggregation.limit(pageSize);

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                taskOperation,
                sortOperation,
                skipOperation,
                limitOperation
        );

        AggregationResults<Task> results = mongoTemplate.aggregate(aggregation, "tasks", Task.class);
        return results
                .getMappedResults()
                .stream()
                .map(converterService::convertToUserTaskDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponse> getAllTasksForUser(String groupId, String userId, Pageable pageable, TaskStatus taskStatus, String search) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new NoGroupFoundException("There is no group, so no tasks as well"));
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();

        Criteria criteria = Criteria.where("groupId").is(groupId);
        criteria.and("userId").ne(Helper.getLoggedInUserId());
        criteria.and("taskStatus").is(taskStatus);

        MatchOperation matchOperation = Aggregation.match(criteria);

        ProjectionOperation taskOperation = Aggregation.project()
                .and("id").as("taskId")
                .and("userId").as("userId")
                .and ("groupId").as("groupId")
                .and("projectId").as("projectId")
                .and("name").as("name")
                .and("description").as("description")
                .and("startDate").as("startDate")
                .and("endDate").as("endDate");

        SortOperation sortOperation = Aggregation.sort(Sort.by(Sort.Direction.DESC, "createdAt"));
        SkipOperation skipOperation = Aggregation.skip((long) pageNumber * pageSize);
        LimitOperation limitOperation = Aggregation.limit(pageSize);

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                taskOperation,
                sortOperation,
                skipOperation,
                limitOperation
        );

        AggregationResults<Task> results = mongoTemplate.aggregate(aggregation, "tasks", Task.class);
        return results
                .getMappedResults()
                .stream()
                .map(converterService::convertToUserTaskDto)
                .collect(Collectors.toList());

    }


    /**
     * Method to fetch all users tasks no matter the group....
     * @param userId
     * @param pageable
     * @return
     */
    @Override
    public List<TaskResponse> getAllUserTasks(String userId, Pageable pageable) {
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();

        Criteria criteria = Criteria.where("userId").ne(Helper.getLoggedInUserId());


        MatchOperation matchOperation = Aggregation.match(criteria);

        ProjectionOperation taskOperation = Aggregation.project()
                .and("id").as("taskId")
                .and("userId").as("userId")
                .and ("groupId").as("groupId")
                .and("projectId").as("projectId")
                .and("name").as("name")
                .and("description").as("description")
                .and("startDate").as("startDate")
                .and("endDate").as("endDate");

        SortOperation sortOperation = Aggregation.sort(Sort.by(Sort.Direction.DESC, "createdAt"));
        SkipOperation skipOperation = Aggregation.skip((long) pageNumber * pageSize);
        LimitOperation limitOperation = Aggregation.limit(pageSize);

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                taskOperation,
                sortOperation,
                skipOperation,
                limitOperation
        );

        AggregationResults<Task> results = mongoTemplate.aggregate(aggregation, "tasks", Task.class);
        return results
                .getMappedResults()
                .stream()
                .map(converterService::convertToUserTaskDto)
                .collect(Collectors.toList());
    }

    @Override
    public String updateTaskStatus(String id, TaskStatus taskStatus) {
        Task task = taskRepository.getById(id).orElseThrow(() -> new NoTaskFoundException("There is no task with that id, so it cannot be updated!"));
        task.setStatus(taskStatus);
        return "Success";
    }

    @Override
    public List<TaskResponse> getActiveTasksByGroupId(String groupId, Pageable pageable) {
        // Maybe needed later ...
        return null;
    }

    @Override
    public List<TaskResponse> getActiveTasksByGroupIdAndUserId(String groupId, String userId, Pageable pageable) {
        //Maybe needed later...
        return null;
    }
}
