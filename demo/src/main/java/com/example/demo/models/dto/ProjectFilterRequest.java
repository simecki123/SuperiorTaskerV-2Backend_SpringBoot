package com.example.demo.models.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectFilterRequest {
    @NotBlank
    private String name;

    @NotBlank
    private Double completion;

    @NotBlank
    @DateTimeFormat
    private LocalDateTime startDate;

    @NotBlank
    @DateTimeFormat
    private LocalDateTime endDate;

}
