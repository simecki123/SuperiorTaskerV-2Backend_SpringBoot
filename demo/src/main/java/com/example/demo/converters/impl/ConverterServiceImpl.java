package com.example.demo.converters.impl;

import com.example.demo.models.enums.Role;
import com.example.demo.repository.UserGroupRelationRepository;
import com.example.demo.service.AmazonS3Service;
import com.example.demo.converters.ConverterService;
import com.example.demo.models.dao.*;
import com.example.demo.models.dto.*;
import com.example.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.net.URL;
import java.util.List;

@Service
@AllArgsConstructor
public class ConverterServiceImpl implements ConverterService {
    private final UserRepository userProfileRepository;
    private final AmazonS3Service amazonS3Service;

    private final UserGroupRelationRepository userGroupRelationRepository;

    @Override
    public UserDto convertToUserDto(User user, List<UserGroupRelation> userGroupRelationList) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setDescription(user.getDescription());
        userDto.setProfileUri(convertPhotoUriToUrl(user.getPhotoUri()));
        userDto.setGroupMembershipData(userGroupRelationList);

        return userDto;
    }

    @Override
    public GroupDto convertToGroupDto(Group group) {
        GroupDto groupDto = new GroupDto();
        groupDto.setId(group.getId());
        groupDto.setName(group.getName());
        groupDto.setDescription(group.getDescription());
        groupDto.setPhotoUri(convertPhotoUriToUrl(group.getPhotoUri()));
        return groupDto;
    }

    @Override
    public GroupMemberResponse convertToGroupMemberResponse(GroupMemberDto groupMemberDto) {
        GroupMemberResponse response = new GroupMemberResponse();
        response.setUserId(groupMemberDto.getUserId());
        response.setFirstName(groupMemberDto.getFirstName());
        response.setLastName(groupMemberDto.getLastName());
        response.setRole(groupMemberDto.getRole());
        response.setPhotoUrl(convertPhotoUriToUrl(groupMemberDto.getPhotoUrl()));
        return response;
    }

    @Override
    public GroupMemberResponse convertUserToGroupMemberResponse(User user, Role role) {
        GroupMemberResponse response = new GroupMemberResponse();
        response.setUserId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setDescription(user.getDescription());
        response.setRole(role);
        response.setPhotoUrl(convertPhotoUriToUrl(user.getPhotoUri()));
        return response;
    }

    @Override
    public UserProfileEditResponse convertToUserProfileResponse(UserProfileRequest userProfileRequest) {
        UserProfileEditResponse response = new UserProfileEditResponse();
        String url = convertPhotoUriToUrl(userProfileRequest.getPhotoUrl());
        response.setProfileUri(url);
        return response;
    }

    @Override
    public UserToAddInGroupResponse convertUserToUserToAddInGroupResponse(User user) {
        UserToAddInGroupResponse response = new UserToAddInGroupResponse();
        response.setUserId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setDescription(user.getDescription());
        response.setPhotoUrl(user.getPhotoUri());
        return response;
    }


    @Override
    public MessageResponse convertToMessageResponse(Message message) {
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setId(message.getId());
        messageResponse.setMessage(message.getMessage());
        messageResponse.setMessageStatus(message.getMessageStatus());
        messageResponse.setFirstName(message.getFirstName());
        messageResponse.setLastName(message.getLastName());
        messageResponse.setPhotoUri(message.getPhotoUri());
        return messageResponse;
    }

    @Override
    public UserGroupRelationDto convertToUserGroupRelation(UserGroupRelation userGroupRelation) {
        UserGroupRelationDto dto = new UserGroupRelationDto();
        dto.setGroupId(userGroupRelation.getGroupId());
        dto.setUserId(userGroupRelation.getUserId());
        dto.setUserRole(userGroupRelation.getRole());
        return dto;
    }

    @Override
    public ProjectResponse convertToUserProjectDto(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setGroupId(project.getGroupId());
        response.setUserId(project.getUserId());
        response.setStartDate(project.getStartDate());
        response.setEndDate(project.getEndDate());
        response.setCompletion(project.getCompletion());
        return response;
    }

    @Override
    public TaskResponse convertToUserTaskDto(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setUserId(task.getUserId());
        response.setGroupId(task.getGroupId());
        response.setProjectId(task.getProjectId());
        response.setName(task.getName());
        response.setDescription(task.getDescription());
        response.setTaskStatus(task.getStatus());
        response.setStartDate(task.getStartDate());
        response.setEndDate(task.getEndDate());
        return response;
    }

    @Override
    public String convertPhotoUriToUrl(String photoUri) {
        if (photoUri != null && !photoUri.isEmpty()) {
            URL presignedUrl = amazonS3Service.generatePresignedUrl(photoUri);
            return presignedUrl.toString();
        } else {
            return null;
        }
    }
}