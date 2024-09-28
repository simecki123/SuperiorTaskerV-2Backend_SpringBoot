package com.example.demo.models.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectDto {
    private String id;
    private String name;
    private String groupId;
    private String userId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double completion;
}
