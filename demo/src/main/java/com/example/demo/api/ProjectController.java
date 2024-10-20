package com.example.demo.api;

import com.example.demo.exceptions.NoGroupFoundException;
import com.example.demo.exceptions.NoProjectFoundException;
import com.example.demo.models.dto.*;
import com.example.demo.service.ProjectService;
import com.example.demo.config.openapi.ShowAPI;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("api/projects")
@Slf4j
@AllArgsConstructor
@ShowAPI
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping("/createProject")
    public ResponseEntity<ProjectResponse> crateGroup(@RequestBody ProjectRequest projectRequest) {
        try {
            log.info("Creating new project...");
            return ResponseEntity.ok(projectService.createProject(projectRequest));
        }catch (NoGroupFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Error e) {
            return ResponseEntity.badRequest().build();
        }

    }

    @GetMapping("/getFilteredProjects")
    public ResponseEntity<List<ProjectResponse>> getFilteredProjects(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String groupId,
            @RequestParam(required = false) Double startCompletion,  // Changed to Double object
            @RequestParam(required = false) Double endCompletion,    // Added end range
            @RequestParam(required = false) Boolean includeComplete, // Option to include 100% complete
            @RequestParam(required = false) Boolean includeNotStarted, // Option to include 0%
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size
    ) {
        log.info("Received request with userId: {}", userId);
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<ProjectResponse> projects = projectService.getAllProjects(
                    userId, groupId, startCompletion, endCompletion,
                    includeComplete, includeNotStarted, search, pageable);

            if (projects.isEmpty()) {
                log.info("No projects found for userId: {}", userId);
                return ResponseEntity.ok(Collections.emptyList());
            }

            log.info("Found {} projects", projects.size());
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            log.error("Error in getFilteredProjects: ", e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete-project")
    public ResponseEntity<String> deleteProject(@RequestParam String projectId) {
        try {

            log.info("Deleting project by his id...");
            return ResponseEntity.ok(projectService.deleteProjectById(projectId));
        }catch (NoProjectFoundException e ){
            log.error("Error in finding project with that id");
            return ResponseEntity.notFound().build();
        }catch (Error e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
