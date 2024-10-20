package com.example.demo.api;

import com.example.demo.exceptions.NoGroupFoundException;
import com.example.demo.exceptions.NoProjectFoundException;
import com.example.demo.exceptions.NoTaskFoundException;
import com.example.demo.models.dto.TaskRequest;
import com.example.demo.models.dto.TaskResponse;
import com.example.demo.models.enums.TaskStatus;
import com.example.demo.service.TaskService;
import com.example.demo.config.openapi.ShowAPI;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @GetMapping("/getFilteredTasks")
    public ResponseEntity<List<TaskResponse>> getFilteredTasks(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String groupId,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            List<TaskResponse> tasks = taskService.getAllTasksForUser(userId, groupId, projectId, status, search, pageable);

            // Add debug logging
            log.info("Retrieved tasks count: {}", tasks.size());
            log.info("Query parameters - userId: {}, groupId: {}, projectId: {}, status: {}, search: {}, page: {}, size: {}",
                    userId, groupId, projectId, status, search, page, size);

            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            log.error("Error fetching filtered tasks", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/update-task")
    public ResponseEntity<String> updateTaskStatus(@RequestParam String taskId, @RequestParam TaskStatus taskStatus) {
        try {
            log.info("Updating task status...");
            return ResponseEntity.ok(taskService.updateTaskStatus(taskId, taskStatus));

        } catch (NoTaskFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/change-task-user")
    public ResponseEntity<String> changeUserOfTheTask(@RequestParam String taskId, @RequestParam String newUserId){
        return null;
    }

    @DeleteMapping("/delete-task")
    public ResponseEntity<String> deleteTask(@RequestParam String taskId) {
        try {
            log.info("Deleting task...");
            return ResponseEntity.ok(taskService.deleteTaskById(taskId));
        } catch (NoTaskFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }



}
