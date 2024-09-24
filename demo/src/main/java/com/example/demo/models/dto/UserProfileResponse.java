package com.example.demo.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private String email;
    private String firstName;
    private String lastName;
    private String description;
    private String profileUri;
    private List<UserGroupRelationDto> groupMembershipData;
}
