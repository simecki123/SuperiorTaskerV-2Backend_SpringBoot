package com.example.demo.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileEditResponse {
    private String photoUri;

    public void setPhotoUrl(String convertPhotoUriToUrl) {
    }
}
