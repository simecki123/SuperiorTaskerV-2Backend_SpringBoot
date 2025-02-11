package com.example.demo.models.dto;

import com.example.demo.models.dao.UserGroupRelation;
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
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String description;
    private String profileUri;
    private List<UserGroupRelation> groupMembershipData;
}
