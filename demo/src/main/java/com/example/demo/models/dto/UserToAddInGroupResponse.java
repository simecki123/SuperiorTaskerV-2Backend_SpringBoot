package com.example.demo.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserToAddInGroupResponse {
    private String userId;
    private String firstName;
    private String lastName;
    private String description;
    private String photoUrl;
}
