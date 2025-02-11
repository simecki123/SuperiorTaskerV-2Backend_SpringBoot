package com.example.demo.models.dto;

import com.example.demo.models.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberResponse {
    private String userId;
    private String firstName;
    private String lastName;
    private String description;
    private Role role;
    private String photoUrl;
}
