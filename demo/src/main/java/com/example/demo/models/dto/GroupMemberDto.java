package com.example.demo.models.dto;

import com.example.demo.models.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberDto {
    private String userId;
    private String firstName;
    private String lastName;
    private List<Role> roles;
    private String photoUrl;
}
