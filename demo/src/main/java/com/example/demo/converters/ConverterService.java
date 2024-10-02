package com.example.demo.converters;

import com.example.demo.models.dao.*;
import com.example.demo.models.dto.*;

public interface ConverterService {
    UserDto convertToUserDto(User user);
    GroupDto convertToGroupDto(Group group);
    GroupMemberResponse convertToGroupMemberResponse(GroupMemberDto groupMemberDto);
    UserProfileEditResponse convertToUserProfileResponse(UserProfileRequest userProfileRequest);
    GroupDto convertToUserDto(Group group);
    MessageDto convertToMessageDto(Message message);
    UserGroupRelationDto convertToUserGroupRelation(UserGroupRelation userGroupRelation);
    ProjectResponse convertToUserProjectDto(Project project);
    TaskResponse convertToUserTaskDto(Task task);
    String convertPhotoUriToUrl(String photoUri);



}
