package com.example.demo.models.dto;

import com.example.demo.models.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberDto {
    private String userId;
    private String firstName;
    private String lastName;
    private Role role;
    private String photoUrl;
}
