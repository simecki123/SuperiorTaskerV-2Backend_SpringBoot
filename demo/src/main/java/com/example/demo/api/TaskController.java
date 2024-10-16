package com.example.demo.api;

import com.example.demo.exceptions.NoGroupFoundException;
import com.example.demo.exceptions.NoProjectFoundException;
import com.example.demo.models.dto.TaskRequest;
import com.example.demo.models.dto.TaskResponse;
import com.example.demo.service.TaskService;
import com.example.demo.config.openapi.ShowAPI;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/tasks")
@Slf4j
@AllArgsConstructor
@ShowAPI
public class TaskController {
    private final TaskService taskService;

    @PostMapping("/createTask")
    public ResponseEntity<TaskResponse> createTask(@RequestBody TaskRequest taskRequest) {
        try {
            log.info("Creating new task...");
            return ResponseEntity.ok(taskService.createTask(taskRequest));
        }catch (NoProjectFoundException | NoGroupFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Error e) {
            return ResponseEntity.badRequest().build();
        }

    }

    @PostMapping("/getAllTasksByUserId")
    public ResponseEntity<List<TaskResponse>> getAllTasksByUserId(
            @RequestBody String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size
            ) {
        try {
            log.info("Get all tasks from user...");
            return ResponseEntity.ok(taskService.getAllUserTasks(userId, PageRequest.of(page, size)));

        }catch (Error e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }

    }


}
