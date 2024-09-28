package com.example.demo.models.dto;

import com.example.demo.models.enums.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserGroupRelationDto {
    private String groupId;
    private String userId;
    private Role userRole;
}
