package com.example.demo.models.dto;

import com.example.demo.models.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskRequest {
    @NotBlank
    @Size(max = 50)
    private String userId;

    @NotBlank
    @Size(max = 50)
    private String projectId;

    @NotBlank
    @Size(max = 50)
    private String groupId;

    @NotBlank
    @Size(max = 50)
    private String name;

    @Size(max = 120)
    private String description;

    @NotBlank
    @DateTimeFormat
    private LocalDateTime startDate;

    @NotBlank
    @DateTimeFormat
    private LocalDateTime endDate;


}
