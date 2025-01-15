package com.example.demo.service.impl;

import com.example.demo.converters.ConverterService;
import com.example.demo.exceptions.NoGroupFoundException;
import com.example.demo.exceptions.NoProjectFoundException;
import com.example.demo.models.dao.Group;
import com.example.demo.models.dao.Project;
import com.example.demo.models.dao.Task;
import com.example.demo.models.dto.*;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ConverterService converterService;
    private final TaskService taskService;
    private final MongoTemplate mongoTemplate;
    private final TaskRepository taskRepository;
    private final GroupRepository groupRepository;

    @Override
    public ProjectResponse createProject(ProjectRequest request) {
        groupRepository.findById(request.getGroupId()).orElseThrow(() -> new NoGroupFoundException("No group associated with the groupId"));

        Project project = new Project();
        project.setGroupId(request.getGroupId());
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setCompletion(0.00);

        projectRepository.save(project);

        log.info("Project created");
        return converterService.convertToUserProjectDto(project);

    }

    @Override
    public List<ProjectResponse> getAllProjects(String userId, String groupId,
                                                Double startCompletion, Double endCompletion,
                                                Boolean includeComplete, Boolean includeNotStarted,
                                                String search, Pageable pageable) {
        Criteria criteria = new Criteria();

        // Remove userId from criteria since it's now handled through tasks
        if (groupId != null && !groupId.isEmpty()) {
            criteria.and("groupId").is(groupId);
        }
        if (search != null && !search.isEmpty()) {
            criteria.and("name").regex(search, "i");
        }

        // Completion filtering
        List<Criteria> completionCriteria = new ArrayList<>();

        if (Boolean.TRUE.equals(includeComplete)) {
            completionCriteria.add(Criteria.where("completion").is(100.0));
        }
        if (Boolean.TRUE.equals(includeNotStarted)) {
            completionCriteria.add(Criteria.where("completion").is(0.0));
        }

        if (startCompletion != null || endCompletion != null) {
            Criteria rangeCriteria = Criteria.where("completion");
            if (startCompletion != null) {
                rangeCriteria.gte(startCompletion);
            }
            if (endCompletion != null) {
                rangeCriteria.lte(endCompletion);
            }
            completionCriteria.add(rangeCriteria);
        }

        if (!completionCriteria.isEmpty()) {
            criteria.orOperator(completionCriteria.toArray(new Criteria[0]));
        }

        MatchOperation matchOperation = Aggregation.match(criteria);
        ProjectionOperation taskOperation = Aggregation.project()
                .and("id").as("projectId")
                .and("groupId").as("groupId")
                .and("name").as("name")
                .and("description").as("description")
                .and("completion").as("completion")
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

        AggregationResults<Project> results = mongoTemplate.aggregate(aggregation, "projects", Project.class);
        log.info("Found {} results", results.getMappedResults().size());

        // If userId is provided, filter projects based on task relationship
        if (userId != null && !userId.isEmpty()) {
            return results.getMappedResults().stream()
                    .filter(project -> {
                        // Check if user has any task in this project
                        UserProjectRelationRequest request = new UserProjectRelationRequest();
                        request.setUserId(userId);
                        request.setProjectId(project.getId());
                        request.setGroupId(project.getGroupId());

                        List<UserProjectResponse> response = taskService.fetchUserProjectRelations(
                                Collections.singletonList(request));
                        return !response.isEmpty();
                    })
                    .map(converterService::convertToUserProjectDto)
                    .collect(Collectors.toList());
        }

        // If no userId provided, return all projects
        return results.getMappedResults()
                .stream()
                .map(converterService::convertToUserProjectDto)
                .collect(Collectors.toList());
    }

    @Override
    public DeleteProjectResponse deleteProjectById(String projectId) {
        try {
            Project project = projectRepository.findById(projectId).orElseThrow(() -> new NoProjectFoundException("There is no project with that id present..."));
            projectRepository.delete(project);
            deleteProjectTasks(projectId);
            DeleteProjectResponse deleteProjectResponse = new DeleteProjectResponse(true, "Project and his tasks deleted successfully...");

            log.info("Project is deleted as well as his tasks...");
            return deleteProjectResponse;
            
        } catch (NoProjectFoundException e) {
            log.error("Project not found: {}", projectId);
            throw e;
        } catch (Exception e) {
            log.error("Error deleting project {}: {}", projectId, e.getMessage());
            throw new RuntimeException("Failed to delete project: " + e.getMessage());
        }


    }

    @Override
    public String deleteProjectByGroupId(String groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new NoGroupFoundException("There is no group with this id ..."));
        List<Project> projectList = projectRepository.findAllByGroupId(groupId);
        for (Project project : projectList){
            projectRepository.delete(project);
            deleteProjectTasks(project.getId());
        }
        log.info("Deleting all projects that belong to that group...");
        return "AllProjects of the group are deleted";
    }


    // Helper method that will delete all project tasks if needed...
    private void deleteProjectTasks(String projectId){
        List<Task> projectTasks = taskRepository.findAllByProjectId(projectId);
        for(Task task : projectTasks) {
            taskRepository.delete(task);
        }
    }




}
