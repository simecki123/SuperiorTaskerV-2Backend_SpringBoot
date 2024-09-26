package com.example.demo.converters;

import com.example.demo.models.dao.*;
import com.example.demo.models.dto.*;

public interface ConverterService {
    UserDto convertToUserDto(User user);
    UserProfileEditResponse convertToUserProfileResponse(UserProfileRequest userProfileRequest);
    GroupDto convertToUserDto(Group group);
    MessageDto convertToMessageDto(Message message);
    UserGroupRelationDto convertToUserGroupRelation(UserGroupRelation userGroupRelation);
    ProjectDto convertToUseProjectDto(Project project);
    TaskDto convertToUseTaskDto(Task task);
    String convertPhotoUriToUrl(String photoUri);




}
