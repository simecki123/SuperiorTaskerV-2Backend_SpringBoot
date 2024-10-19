package com.example.demo.service.impl;

import com.example.demo.converters.ConverterService;
import com.example.demo.exceptions.NoGroupFoundException;
import com.example.demo.exceptions.NoProjectFoundException;
import com.example.demo.models.dao.Group;
import com.example.demo.models.dao.Project;
import com.example.demo.models.dao.Task;
import com.example.demo.models.dto.ProjectRequest;
import com.example.demo.models.dto.ProjectResponse;
import com.example.demo.models.dto.TaskResponse;
import com.example.demo.models.enums.TaskStatus;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.TaskRepository;
import com.example.demo.service.ProjectService;
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
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ConverterService converterService;
    private final MongoTemplate mongoTemplate;
    private final TaskRepository taskRepository;
    private final GroupRepository groupRepository;

    @Override
    public ProjectResponse createProject(ProjectRequest request) {
        groupRepository.findById(request.getGroupId()).orElseThrow(() -> new NoGroupFoundException("No group associated with the groupId"));



        Project project = new Project();
        project.setUserId(request.getUserid());
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
    public void updateProjectCompletion(String id) {

        Project project = projectRepository.findById(id).orElseThrow(() -> new NoProjectFoundException("No project with associated id"));
        log.info("Fetching project ");

        List<Task> taskList = taskRepository.findAllByProjectId(id);
        log.info("returning task list ");
        double completionSum = 0.00;
        for(Task task : taskList) {
            if(task.getStatus().equals(TaskStatus.COMPLETED)){
                completionSum +=1;
            }
        }

        double completion = round((completionSum / taskList.size()) * 100);


        project.setCompletion(completion);

        projectRepository.save(project);


    }

    @Override
    public List<ProjectResponse> getAllProjects(String userId, String groupId,
                                                Double startCompletion, Double endCompletion,
                                                Boolean includeComplete, Boolean includeNotStarted,
                                                String search, Pageable pageable) {

        Criteria criteria = new Criteria();
        criteria.and("userId").is(userId);

        // Handle groupId filter
        if (groupId != null && !groupId.isEmpty()) {
            criteria.and("groupId").is(groupId);
        }

        // Handle search filter
        if (search != null && !search.isEmpty()) {
            criteria.and("name").regex(search, "i");
        }

        // Handle completion criteria
        List<Criteria> completionRanges = new ArrayList<>();

        // Check for completed or not started projects first
        if (Boolean.TRUE.equals(includeComplete)) {
            // Only show 100% complete projects
            completionRanges.add(Criteria.where("completion").is(100.0));
        } else if (Boolean.TRUE.equals(includeNotStarted)) {
            // Only show 0% complete projects
            completionRanges.add(Criteria.where("completion").is(0.0));
        } else if (startCompletion != null || endCompletion != null) {
            // Only apply range filter if neither includeComplete nor includeNotStarted is true
            if (startCompletion != null && endCompletion != null) {
                completionRanges.add(Criteria.where("completion").gt(0.0).lt(100.0)
                        .andOperator(
                                Criteria.where("completion").gte(startCompletion),
                                Criteria.where("completion").lte(endCompletion)
                        ));
            } else if (startCompletion != null) {
                completionRanges.add(Criteria.where("completion").gt(0.0).lt(100.0)
                        .and("completion").gte(startCompletion));
            } else {
                completionRanges.add(Criteria.where("completion").gt(0.0).lt(100.0)
                        .and("completion").lte(endCompletion));
            }
        }

        // Add completion criteria if any exists
        if (!completionRanges.isEmpty()) {
            criteria.orOperator(completionRanges.toArray(new Criteria[0]));
        }

        MatchOperation matchOperation = Aggregation.match(criteria);

        ProjectionOperation taskOperation = Aggregation.project()
                .and("id").as("projectId")
                .and("userId").as("userId")
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

        return results.getMappedResults()
                .stream()
                .map(converterService::convertToUserProjectDto)
                .collect(Collectors.toList());
    }

    @Override
    public String deleteProjectById(String projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NoProjectFoundException("There is no project with that id present..."));
        projectRepository.delete(project);
        deleteProjectTasks(projectId);

        log.info("Project is deleted as well as his tasks...");
        return "Project and his tasks deleted successfully...";


    }

    @Override
    public String deleteProjectByGroupId(String groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new NoGroupFoundException("There is no group with this id ..."));
        List<Project> projectList = projectRepository.findAllByGroupId(groupId);
        for (Project project : projectList){
            projectRepository.delete(project);
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



    // helper method to round completion decimal to 2 decimal points...
    private double round(double value) {

        long factor = (long) Math.pow(10, 2);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


}
