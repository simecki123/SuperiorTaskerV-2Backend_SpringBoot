package com.example.demo.service;

import com.example.demo.models.dto.DeleteResponse;
import com.example.demo.models.dto.ProjectRequest;
import com.example.demo.models.dto.ProjectResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjectService {
    ProjectResponse createProject(ProjectRequest request);

    List<ProjectResponse> getAllProjects(String userId, String groupId,
                                         Double startCompletion, Double endCompletion,
                                         Boolean includeComplete, Boolean includeNotStarted,
                                         String search, Pageable pageable);

    List<ProjectResponse> findAllProjectsByUserId(String userId);

    ProjectResponse getProjectById(String id);

    DeleteResponse deleteProjectById(String projectId);

    ProjectResponse updateProject(ProjectRequest request, String projectId);
    String deleteProjectByGroupId(String groupId);

}
