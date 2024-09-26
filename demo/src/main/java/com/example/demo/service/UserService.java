package com.example.demo.service;

import com.example.demo.models.dto.UserDto;
import com.example.demo.models.dto.UserProfileEditResponse;
import com.example.demo.models.dto.UserProfileRequest;
import com.example.demo.models.dto.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {
    UserDto getUserById(String id);
    boolean checkEmail(String email);
    boolean isUserVerified(String email);
    byte[] downloadUserProfilePhoto() throws IOException;
    void updateFcmToken(String token);
    UserProfileResponse createUser(UserProfileRequest request, MultipartFile photoFile) throws IOException;
    UserProfileEditResponse editUserProfile(String firstName, String lastName, String description, MultipartFile photoFile);

    String getUserIdByEmail(String email);

}
