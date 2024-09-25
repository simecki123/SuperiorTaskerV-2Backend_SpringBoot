package com.example.demo.Service.impl;

import com.example.demo.Service.UserService;
import com.example.demo.converters.ConverterService;
import com.example.demo.models.dto.UserDto;
import com.example.demo.models.dto.UserProfileEditResponse;
import com.example.demo.models.dto.UserProfileRequest;
import com.example.demo.models.dto.UserProfileResponse;
import com.example.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ConverterService converterService;

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
