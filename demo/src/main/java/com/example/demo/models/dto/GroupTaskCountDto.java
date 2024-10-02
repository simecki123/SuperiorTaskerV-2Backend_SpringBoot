package com.example.demo.models.dto;

import com.example.demo.models.enums.TaskStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupTaskCountDto {
    private TaskStatus name;
    private int value;
}
