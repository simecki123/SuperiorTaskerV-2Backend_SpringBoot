package com.example.demo.models.dto;

import com.example.demo.models.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskResponse {
    private String id;
    private String userId;
    private String groupId;
    private String name;
    private String description;
    private TaskStatus taskStatus;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

}
