package com.example.demo.api;

import com.example.demo.exceptions.NoGroupFoundException;
import com.example.demo.models.dto.GroupRequest;
import com.example.demo.models.dto.GroupResponse;
import com.example.demo.models.dto.ProjectRequest;
import com.example.demo.models.dto.ProjectResponse;
import com.example.demo.service.ProjectService;
import com.example.demo.config.openapi.ShowAPI;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
