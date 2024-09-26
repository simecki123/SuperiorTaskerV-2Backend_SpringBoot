package com.example.demo.service.impl;

import com.example.demo.models.dto.*;
import com.example.demo.models.enums.Role;
import com.example.demo.service.GroupService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.print.Pageable;
import java.util.List;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class GroupServiceImpl implements GroupService {
    @Override
    public GroupResponse createGroup(GroupRequest request) {
        return null;
    }

    @Override
    public GroupDto getGroupById(String id) {
        return null;
    }

    @Override
    public GroupResponse joinGroup(GroupRequest request) {
        return null;
    }

    @Override
    public List<GroupProjectCountDto> countGroupProjects(String groupId) {
        return null;
    }

    @Override
    public List<GroupTaskCountDto> countGroupTasks(String groupId) {
        return null;
    }

    @Override
    public GroupEditResponse editGroupInfo(String groupId, String name, String description, MultipartFile photoFile) {
        return null;
    }

    @Override
    public List<GroupDto> getProfileGroups() {
        return null;
    }

    @Override
    public UserGroupRelationDto getGroupRoles(String groupId) {
        return null;
    }

    @Override
    public List<GroupMemberResponse> getGroupMembers(String groupId, Pageable pageable) {
        return null;
    }

    @Override
    public void kickUserFromGroup(String groupId, String userProfileId) {

    }

    @Override
    public void promoteUser(String groupId, String userProfileId, Role role) {

    }
}
