package com.example.demo.converters;

import com.example.demo.models.dao.*;
import com.example.demo.models.dto.*;
import com.example.demo.models.enums.Role;

public interface ConverterService {
    UserDto convertToUserDto(User user);
    GroupDto convertToGroupDto(Group group);
    GroupMemberResponse convertToGroupMemberResponse(GroupMemberDto groupMemberDto);
    UserProfileEditResponse convertToUserProfileResponse(UserProfileRequest userProfileRequest);
    UserToAddInGroupResponse convertUserToUserToAddInGroupResponse(User user);
    GroupMemberResponse convertUserToGroupMemberResponse(User user, Role role);
    MessageResponse convertToMessageResponse(Message message);
    UserGroupRelationDto convertToUserGroupRelation(UserGroupRelation userGroupRelation);
    ProjectResponse convertToUserProjectDto(Project project);
    TaskResponse convertToUserTaskDto(Task task);
    String convertPhotoUriToUrl(String photoUri);



}
