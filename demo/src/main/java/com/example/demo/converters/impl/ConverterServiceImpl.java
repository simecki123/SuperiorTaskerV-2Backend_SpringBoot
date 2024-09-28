package com.example.demo.converters.impl;

import com.example.demo.service.AmazonS3Service;
import com.example.demo.converters.ConverterService;
import com.example.demo.models.dao.*;
import com.example.demo.models.dto.*;
import com.example.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ConverterServiceImpl implements ConverterService {
    private final UserRepository userProfileRepository;

    private final AmazonS3Service amazonS3Service;
    @Override
    public UserDto convertToUserDto(User user) {
        return null;
    }

    @Override
    public UserProfileEditResponse convertToUserProfileResponse(UserProfileRequest userProfileRequest) {
        return null;
    }

    @Override
    public GroupDto convertToUserDto(Group group) {
        return null;
    }

    @Override
    public MessageDto convertToMessageDto(Message message) {
        return null;
    }

    @Override
    public UserGroupRelationDto convertToUserGroupRelation(UserGroupRelation userGroupRelation) {
        return null;
    }

    @Override
    public ProjectDto convertToUseProjectDto(Project project) {
        return null;
    }

    @Override
    public TaskDto convertToUseTaskDto(Task task) {
        return null;
    }

    @Override
    public String convertPhotoUriToUrl(String photoUri) {
        return null;
    }
}
