package com.example.demo.service.impl;

import com.amazonaws.services.kms.model.NotFoundException;
import com.example.demo.converters.ConverterService;
import com.example.demo.exceptions.NoGroupFoundException;
import com.example.demo.exceptions.NoProjectFoundException;
import com.example.demo.models.dao.Project;
import com.example.demo.models.dao.Task;
import com.example.demo.models.dao.User;
import com.example.demo.models.dto.ProjectDto;
import com.example.demo.models.dto.ProjectFilterRequest;
import com.example.demo.models.dto.ProjectRequest;
import com.example.demo.models.dto.ProjectResponse;
import com.example.demo.models.enums.TaskStatus;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.utils.Helper;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ConverterService converterService;
    private final MongoTemplate mongoTemplate;
    private final TaskRepository taskRepository;
    private final GroupRepository groupRepository;

    @Override
    public ProjectResponse createProject(String groupId, ProjectRequest request) {
        groupRepository.findById(groupId).orElseThrow(() -> new NoGroupFoundException("No group associated with the groupId"));
        User user = userRepository.getUserById(Helper.getLoggedInUserId());


        Project project = new Project();
        project.setUserId(request.getUserid());
        project.setGroupId(groupId);
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
    public ProjectResponse getProjectById(String id) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new NotFoundException("No projects associated with the id"));
        log.info("Get project by id finished");
        return converterService.convertToUserProjectDto(project);
    }

    @Override
    public List<ProjectResponse> filterProjects(String groupId, Pageable pageable, String search, ProjectFilterRequest request) {
        groupRepository.findById(groupId).orElseThrow(() -> new NoGroupFoundException("No group associated with the groupId"));
        LocalDateTime now = LocalDateTime.now();
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();

        Criteria criteria = Criteria.where("groupId").is(groupId);
        criteria.and("userId").ne(Helper.getLoggedInUserId());

        // Add criteria here if needed in the future
        // Here ...

        MatchOperation matchOperation = Aggregation.match(criteria);

        ProjectionOperation projectOperation = Aggregation.project()
                .and("id").as("projectId")
                .and("userId").as("userId")
                .and ("groupId").as("groupId")
                .and("name").as("name")
                .and("description").as("description")
                .and("startDate").as("startDate")
                .and("endDate").as("endDate");

        SortOperation sortOperation = Aggregation.sort(Sort.by(Sort.Direction.DESC, "createdAt"));

        SkipOperation skipOperation = Aggregation.skip((long) pageNumber * pageSize);
        LimitOperation limitOperation = Aggregation.limit(pageSize);

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                projectOperation,
                sortOperation,
                skipOperation,
                limitOperation
        );

        AggregationResults<Project> results = mongoTemplate.aggregate(aggregation, "projects", Project.class);

        return results
                .getMappedResults()
                .stream()
                .map(converterService::convertToUserProjectDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateProjectsJob() {

    }

    @Override
    public String updateProjectCompletion(String id) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new NoProjectFoundException("No project with associated id"));
        String groupId = project.getGroupId();
        List<Task> taskList = new ArrayList<>();
        taskList = taskRepository.findAllByProjectId(id);

        double completionSum = 0.00;
        for(Task task : taskList) {
            if(task.getStatus().equals(TaskStatus.COMPLETED)){
                completionSum +=1;
            }
        }

        double completion = completionSum/ taskList.size();


        project.setCompletion(completion);


        return String.valueOf(completion);
    }

    @Override
    public ProjectDto getActiveProjects(String groupId) {
        // Possible future update...
        return null;
    }

    @Override
    public List<ProjectResponse> getAllProjects(String groupId) {
        List<Project> allProjects = projectRepository.findAllByGroupId(groupId); // here is error remember for later
        List<ProjectResponse> allProjectsDto = new ArrayList<>();
        for (Project project : allProjects) {
            ProjectResponse projectDto = converterService.convertToUserProjectDto(project);
            allProjectsDto.add(projectDto);
        }
        return allProjectsDto;
    }
}
