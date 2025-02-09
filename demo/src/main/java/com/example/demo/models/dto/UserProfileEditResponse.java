package com.example.demo.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileEditResponse {

    private String firstName;
    private String lastName;
    private String description;
    private String profileUri;


}
