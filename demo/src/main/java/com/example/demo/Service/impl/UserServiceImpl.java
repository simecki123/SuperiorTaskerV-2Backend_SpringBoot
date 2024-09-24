package com.example.demo.Service.impl;

import com.example.demo.Service.UserService;
import com.example.demo.models.dto.UserDto;
import com.example.demo.models.dto.UserProfileEditResponse;
import com.example.demo.models.dto.UserProfileRequest;
import com.example.demo.models.dto.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class UserServiceImpl implements UserService {
    @Override
    public UserDto getUserById(String id) {
        return null;
    }

    @Override
    public boolean checkEmail(String email) {
        return false;
    }

    @Override
    public boolean isUserVerified(String email) {
        return false;
    }

    @Override
    public byte[] downloadUserProfilePhoto() throws IOException {
        return new byte[0];
    }

    @Override
    public void updateFcmToken(String token) {

    }

    @Override
    public UserProfileResponse createUser(UserProfileRequest request, MultipartFile photoFile) throws IOException {
        return null;
    }

    @Override
    public UserProfileEditResponse editUserProfile(String firstName, String lastName, String description, MultipartFile photoFile) {
        return null;
    }

    @Override
    public String getUserIdByEmail(String email) {
        return null;
    }


}
