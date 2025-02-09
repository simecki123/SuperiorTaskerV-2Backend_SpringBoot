package com.example.demo.service;

import com.example.demo.models.dto.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface GroupService {
    GroupResponse createGroup(String name, String description, MultipartFile photoFile);
    GroupDto getGroupById(String id);
    GroupEditResponse editGroupInfo(String groupId, String name, String description, MultipartFile photoFile);
    List<GroupDto> getAllUserGroups(String userId, Pageable pageable);
    List<GroupMemberResponse> getGroupMembers(String groupId, Pageable pageable);
    void promoteUser(ChangeGroupAdminDto changeGroupAdminDto);
}
