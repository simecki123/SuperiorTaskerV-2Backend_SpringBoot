package com.example.demo.service;

import com.example.demo.models.dto.UserDto;
import com.example.demo.models.dto.UserProfileEditResponse;
import com.example.demo.models.dto.UserProfileRequest;
import com.example.demo.models.dto.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {
    //user
    UserDto getUserById(String id);
    boolean checkEmail(String email);
    String getUserIdByEmail(String email);

    //userprofile

    byte[] downloadUserProfilePhoto() throws IOException;
    UserProfileEditResponse editUserProfile(String firstName, String lastName, String description, MultipartFile photoFile);
    void updateFcmToken(String token);





}
