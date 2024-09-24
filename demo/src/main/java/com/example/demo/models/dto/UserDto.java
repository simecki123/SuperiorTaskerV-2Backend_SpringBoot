package com.example.demo.models.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    private String email;
    private String firstName;
    private String lastName;
    private String description;
    private String profileUri;
    private List<UserGroupRelationDto> groupMembershipData;
}
