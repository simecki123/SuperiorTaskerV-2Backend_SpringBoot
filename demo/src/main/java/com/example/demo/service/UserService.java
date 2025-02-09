package com.example.demo.service;

import com.example.demo.models.dto.*;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    UserProfileEditResponse editUserProfile(String firstName, String lastName, String description, MultipartFile photoFile);
    List<UserToAddInGroupResponse> fetchUserByNameAndNotHisGroup(String groupId, String search, Pageable pageable);
    List<UserToAddInGroupResponse> fetchUsersOfTheGroupWithText(String groupId, String search, Pageable pageable);

}
