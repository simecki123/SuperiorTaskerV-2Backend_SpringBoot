package com.example.demo.service.impl;

import com.example.demo.exceptions.NoUserFoundException;
import com.example.demo.models.dao.Project;
import com.example.demo.models.dao.Task;
import com.example.demo.models.dto.ProjectResponse;
import com.example.demo.models.dto.UserStatisticsDto;
import com.example.demo.models.enums.TaskStatus;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ProjectService;
import com.example.demo.service.UserStatisticsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class UserStatisticsServiceImpl implements UserStatisticsService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectService projectService;

    @Override
    public UserStatisticsDto getUserStats(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new NoUserFoundException("User with ID " + userId + " not found");
        }

        List<Task> userTasks = taskRepository.findAllByUserId(userId);

        List<ProjectResponse> userProjects = projectService.findAllProjectsByUserId(userId);

        int totalTasks = userTasks.size();
        int completedTasks = (int) userTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .count();
        int inProgressTasks = (int) userTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.IN_PROGRESS)
                .count();

        int totalProjects = userProjects.size();
        int completedProjects = (int) userProjects.stream()
                .filter(project -> project.getCompletion() == 100.0)
                .count();
        int incompleteProjects = totalProjects - completedProjects;

        return new UserStatisticsDto(
                totalTasks,
                completedTasks,
                inProgressTasks,
                totalProjects,
                completedProjects,
                incompleteProjects
        );
    }
}
