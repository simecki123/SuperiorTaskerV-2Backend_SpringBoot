package com.example.demo.models.dto;

import com.example.demo.models.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserGroupRelationResponse {
    private String id;
    private String userId;
    private String groupId;
    private Role role;
}
