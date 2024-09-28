package com.example.demo.service;

import com.example.demo.models.dto.ProjectDto;
import com.example.demo.models.dto.ProjectFilterRequest;
import com.example.demo.models.dto.ProjectRequest;
import com.example.demo.models.dto.ProjectResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjectService {
    ProjectResponse createProject(String groupId, ProjectRequest request);
    ProjectResponse getProjectById(String id);
    List<ProjectResponse> filterProjects(String groupId, Pageable pageable, String search, ProjectFilterRequest request);
    void updateProjectsJob();
    String updateProjectCompletion(String id, double completion);
    ProjectDto getActiveProjects(String groupId);
    ProjectDto getAllProjects(String groupId);

}
