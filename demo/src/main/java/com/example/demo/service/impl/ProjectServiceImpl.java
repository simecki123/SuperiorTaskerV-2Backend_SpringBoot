package com.example.demo.service.impl;

import com.example.demo.converters.ConverterService;
import com.example.demo.exceptions.NoGroupFoundException;
import com.example.demo.exceptions.NoProjectFoundException;
import com.example.demo.models.dao.Project;
import com.example.demo.models.dao.Task;
import com.example.demo.models.dto.*;
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

    /**
     * Method to create new project.
     * @param request Project that needs to be created.
     * @return New project.
     */
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

    /**
     * Method to fetch all Projects.
     * @param userId User.
     * @param groupId Group.
     * @param startCompletion starting value of completion.
     * @param endCompletion end point of completion.
     * @param includeComplete include projects that have completion percentage of 100%.
     * @param includeNotStarted include projects that have completion percentage of 0%.
     * @param search string value of project name that we want to search for.
     * @param pageable pagination.
     * @return list of projects.
     */
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

    /**
     * Method that will find all projects by user id.
     * @param userId User.
     * @return list of projects.
     */
    @Override
    public List<ProjectResponse> findAllProjectsByUserId(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }

        List<Project> allProjects = projectRepository.findAll();

        return allProjects.stream()
                .filter(project -> {
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

    /**
     *  Method that returns project by its id.
     * @param id project Id.
     * @return project.
     */
    @Override
    public ProjectResponse getProjectById(String id) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new NoProjectFoundException("No project found with id: " + id));
        return converterService.convertToUserProjectDto(project);

    }

    @Override
    public DeleteResponse deleteProjectById(String projectId) {
        try {
            Project project = projectRepository.findById(projectId).orElseThrow(() -> new NoProjectFoundException("There is no project with that id present..."));
            projectRepository.delete(project);
            deleteProjectTasks(projectId);
            DeleteResponse deleteProjectResponse = new DeleteResponse(true, "Project and his tasks deleted successfully...");

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

    /**
     * Method that handles update of the project.
     * @param request New project value that existing project will be updated with.
     * @param projectId id of the project that we want to update.
     * @return new updated project.
     */
    @Override
    public ProjectResponse updateProject(ProjectRequest request, String projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NoProjectFoundException("No project found with id: " + projectId));

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());

        projectRepository.save(project);

        log.info("Project updated successfully");
        return converterService.convertToUserProjectDto(project);
    }

    /**
     * Helper method that will delete all project tasks if needed...
     * @param projectId Project id
     */
    private void deleteProjectTasks(String projectId){
        List<Task> projectTasks = taskRepository.findAllByProjectId(projectId);
        for(Task task : projectTasks) {
            taskRepository.delete(task);
        }
    }

}
