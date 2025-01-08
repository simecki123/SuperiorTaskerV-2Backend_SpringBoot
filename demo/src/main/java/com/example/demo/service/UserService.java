package com.example.demo.service;

import com.example.demo.models.dto.*;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {
    //user
    UserDto getUserById(String id);
    boolean checkEmail(String email);
    String getUserIdByEmail(String email);

    //userprofile

    byte[] downloadUserProfilePhoto() throws IOException;
    UserProfileEditResponse editUserProfile(String firstName, String lastName, String description, MultipartFile photoFile);
    List<UserToAddInGroupResponse> fetchUserByNameAndNotHisGroup(String groupId, String search, Pageable pageable);
    void updateFcmToken(String token);





}
