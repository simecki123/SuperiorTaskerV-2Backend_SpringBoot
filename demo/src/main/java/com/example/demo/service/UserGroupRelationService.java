package com.example.demo.service;

import com.example.demo.exceptions.CantKickYourselfException;
import com.example.demo.exceptions.NoUserGroupRelation;
import com.example.demo.exceptions.UserGroupRelationAlreadyExistsException;
import com.example.demo.models.dto.UserGroupRelationDto;
import com.example.demo.models.dto.UserGroupRelationRequest;
import com.example.demo.models.dto.UserGroupRelationResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserGroupRelationService {
    List<UserGroupRelationDto> getMembershipsByUserId(String userId, Pageable pageable);
    List<UserGroupRelationDto> getMembershipsByGroupId(String groupId, Pageable pageable);
    UserGroupRelationResponse createNewUserGroupRelation(String userId, String groupId) throws UserGroupRelationAlreadyExistsException;
    String leaveGroup(String userId, String groupId) throws NoUserGroupRelation;
    String kickUser(String userId, String groupId) throws NoUserGroupRelation, CantKickYourselfException;
}
