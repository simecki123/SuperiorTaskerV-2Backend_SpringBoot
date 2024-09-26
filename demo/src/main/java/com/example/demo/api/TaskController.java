package com.example.demo.api;

import com.example.demo.service.TaskService;
import com.example.demo.config.openapi.ShowAPI;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/tasks")
@Slf4j
@AllArgsConstructor
@ShowAPI
public class TaskController {
    private final TaskService taskService;
}
