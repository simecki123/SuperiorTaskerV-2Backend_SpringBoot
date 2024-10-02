package com.example.demo.service;

import com.example.demo.models.dto.*;
import com.example.demo.models.enums.Role;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface GroupService {
    GroupResponse createGroup(GroupRequest request);
    GroupDto getGroupById(String id);
    GroupResponse joinGroup(GroupRequest request);
    List<GroupProjectCountDto> countGroupProjects(String groupId);
    List<GroupTaskCountDto> countGroupTasks(String groupId);
    GroupEditResponse editGroupInfo(String groupId, String name, String description, MultipartFile photoFile);
    List<GroupDto> getProfileGroups();
    UserGroupRelationDto getGroupRoles(String groupId);
    List<GroupMemberResponse> getGroupMembers(String groupId, Pageable pageable);
    void kickUserFromGroup(String groupId, String userProfileId);
    void promoteUser(String groupId, String userId, Role role);
}
