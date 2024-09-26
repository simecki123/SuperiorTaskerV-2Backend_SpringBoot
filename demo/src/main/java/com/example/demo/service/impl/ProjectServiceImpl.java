package com.example.demo.service.impl;

import com.example.demo.models.dto.ProjectDto;
import com.example.demo.models.dto.ProjectFilterRequest;
import com.example.demo.models.dto.ProjectRequest;
import com.example.demo.models.dto.ProjectResponse;
import com.example.demo.service.ProjectService;
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
public class ProjectServiceImpl implements ProjectService {
    @Override
    public ProjectResponse createProject(String groupId, ProjectRequest request) {
        return null;
    }

    @Override
    public ProjectResponse getProjectById(String id) {
        return null;
    }

    @Override
    public List<ProjectResponse> filterProjects(String groupId, Pageable pageable, String search, ProjectFilterRequest request) {
        return null;
    }

    @Override
    public void updateProjectsJob() {

    }

    @Override
    public String updateProjectCompletion(String id, double completion) {
        return null;
    }

    @Override
    public ProjectDto getActiveProjects(String groupId) {
        return null;
    }

    @Override
    public ProjectDto getAllProjects(String grouPId) {
        return null;
    }
}
