package com.example.demo.converters.impl;

import com.example.demo.repository.UserGroupRelationRepository;
import com.example.demo.service.AmazonS3Service;
import com.example.demo.converters.ConverterService;
import com.example.demo.models.dao.*;
import com.example.demo.models.dto.*;
import com.example.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ConverterServiceImpl implements ConverterService {
    private final UserRepository userProfileRepository;
    private final AmazonS3Service amazonS3Service;

    private final UserGroupRelationRepository userGroupRelationRepository;

    @Override
    public UserDto convertToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setDescription(user.getDescription());
        userDto.setProfileUri(convertPhotoUriToUrl(user.getPhotoUri()));
        List<UserGroupRelation> userGroupRelationList = userGroupRelationRepository.findAllByUserId(user.getId());
        userDto.setGroupMembershipData(userGroupRelationList);

        return userDto;
    }

    @Override
    public GroupDto convertToGroupDto(Group group) {
        GroupDto groupDto = new GroupDto();
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
        response.setRoles(groupMemberDto.getRoles());
        response.setPhotoUrl(convertPhotoUriToUrl(groupMemberDto.getPhotoUrl()));
        return response;
    }

    @Override
    public UserProfileEditResponse convertToUserProfileResponse(UserProfileRequest userProfileRequest) {
        UserProfileEditResponse response = new UserProfileEditResponse();
        // Assuming the photoUri is set somewhere else, possibly after saving the profile
        String url = convertPhotoUriToUrl(userProfileRequest.getPhotoUrl());
        response.setPhotoUri(url);
        return response;
    }

    @Override
    public MessageDto convertToMessageDto(Message message) {
        MessageDto messageDto = new MessageDto();
        messageDto.setProjectId(message.getProjectId());
        messageDto.setGroupId(message.getGroupId());
        messageDto.setMessage(message.getMessage());
        messageDto.setCreatedAt(message.getCreatedAt());
        return messageDto;
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
    public List<UserGroupRelationDto> convertToUserGroupRelation(List<UserGroupRelation> userGroupRelationList) {
        List<UserGroupRelationDto> userGroupRelationDtos = new ArrayList<>();
        for(UserGroupRelation userGroupRelation : userGroupRelationList) {
            UserGroupRelationDto userGroupRelationDto = new UserGroupRelationDto();
            userGroupRelationDto.setUserId(userGroupRelation.getUserId());
            userGroupRelationDto.setGroupId(userGroupRelation.getGroupId());
            userGroupRelationDto.setUserRole(userGroupRelation.getRole());
        }

        return userGroupRelationDtos;
    }

    @Override
    public ProjectResponse convertToUserProjectDto(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
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