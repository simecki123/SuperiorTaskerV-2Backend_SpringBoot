package com.example.demo.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatisticsDto {
    private int numberOfTasks;
    private int numberOfFinishedTasks;
    private int numberOfUnfinishedTasks;

    private int numberOfProjects;
    private int numberOfCompletedProjects;
    private int numberOfIncompleteProjects;
}
