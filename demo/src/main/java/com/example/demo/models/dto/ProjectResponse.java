package com.example.demo.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponse {

    private String id;
    private String name;
    private String description;
    private String groupId;
    private String userId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double completion;
}
