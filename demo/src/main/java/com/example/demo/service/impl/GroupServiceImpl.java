package com.example.demo.service.impl;

import com.amazonaws.services.kms.model.NotFoundException;
import com.example.demo.converters.ConverterService;
import com.example.demo.exceptions.GroupAlreadyExistsException;
import com.example.demo.exceptions.NoGroupFoundException;
import com.example.demo.exceptions.NoUserFoundException;
import com.example.demo.models.dao.Group;
import com.example.demo.models.dao.User;
import com.example.demo.models.dao.UserGroupRelation;
import com.example.demo.models.dto.*;
import com.example.demo.models.enums.Role;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.UserGroupRelationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.services.AuthService;
import com.example.demo.security.utils.Helper;
import com.example.demo.service.AmazonS3Service;
import com.example.demo.service.GroupService;
import com.example.demo.service.UserGroupRelationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Pageable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    private final ConverterService converterService;

    private final MongoTemplate mongoTemplate;

    private final UserGroupRelationRepository groupMembershipRepository;

    private final AmazonS3Service amazonS3Service;

    private final AuthService authService;
    private final UserGroupRelationService userGroupRelationService;


    @Override
    public GroupResponse createGroup(String name, String description , MultipartFile photoFile) {
        Optional<Group> groupOptional = groupRepository.findByName(name);

        if(groupOptional.isPresent()) {
            String existingGroupName = groupOptional.get().getName().toLowerCase();
            String groupNameRequest = name.toLowerCase();
            if (groupNameRequest.equals(existingGroupName)) {
                throw new GroupAlreadyExistsException("Group name already exists: " + name);
            }
        }

        Group group = new Group();
        group.setName(name);
        group.setDescription(description);
        if (photoFile != null) {
            try {
                String path = "groupPhotos";

                amazonS3Service.updateFileInS3(path, group.getId(), photoFile.getInputStream());

                group.setPhotoUri(path + "/" + group.getId());

                log.info("Group photo updated successfully");
            } catch (IOException e) {
                log.error("Error updating the group photo", e);
                throw new RuntimeException("Failed to update the group photo", e);
            }
        }
        groupRepository.save(group);

        //? Saving the relation of the profile and group
        UserGroupRelation groupMembership = new UserGroupRelation();
        groupMembership.setGroupId(group.getId());
        groupMembership.setUserId(Helper.getLoggedInUserId());
        groupMembership.setRole(Role.ADMIN);
        groupMembershipRepository.save(groupMembership);


        GroupResponse response = new GroupResponse();
        response.setGroupId(group.getId());
        response.setName(name);
        response.setDescription(description);

        log.info("Group: ", response);
        log.info("Relation: ", groupMembership);

        log.info("Group created");
        return response;
    }

    @Override
    public GroupDto getGroupById(String id) {
        Group group = groupRepository.findById(id).orElseThrow(() -> new NotFoundException("No group associated with that id"));

        log.info("Get group by id finished");
        return converterService.convertToGroupDto(group);
    }

    @Override
    public GroupResponse joinGroup(GroupRequest request) {
        Group group = groupRepository.findByName(request.getName())
                .orElseThrow(() -> new NotFoundException("Group not found"));

        GroupResponse response = new GroupResponse();
        response.setGroupId(group.getId());
        response.setName(request.getName());

        UserGroupRelation existingMembership = groupMembershipRepository.findByUserIdAndGroupId(Helper.getLoggedInUserId(), group.getId());
        if (existingMembership != null) {
            throw new IllegalStateException("User is already a member of this group");
        }
        //? Saving the relation between a group and the profile
        UserGroupRelation groupMembership = new UserGroupRelation();
        groupMembership.setUserId(Helper.getLoggedInUserId());
        groupMembership.setGroupId(group.getId());

        groupMembershipRepository.save(groupMembership);

        log.info("Group join successful");
        return response;
    }

    @Override
    public List<GroupProjectCountDto> countGroupProjects(String groupId) {
        groupRepository.findById(groupId).orElseThrow(() -> new NoGroupFoundException("No group associated with the groupId"));

        MatchOperation matchByGroupId = Aggregation.match(Criteria.where("groupId").is(groupId));


        ProjectionOperation projectionOperation = Aggregation.project()
                .andInclude("value")
                .andExclude("_id");

        Aggregation aggregation = Aggregation.newAggregation(
                matchByGroupId,
                projectionOperation
        );

        AggregationResults<GroupProjectCountDto> results = mongoTemplate.aggregate(aggregation, "projects", GroupProjectCountDto.class);

        log.info("Fetched the group project count successfully");
        return results.getMappedResults();

    }

    @Override
    public List<GroupTaskCountDto> countGroupTasks(String groupId) {
        groupRepository.findById(groupId).orElseThrow(() -> new NoGroupFoundException("No group associated with the groupId"));

        MatchOperation matchByGroupId = Aggregation.match(Criteria.where("groupId").is(groupId));


        ProjectionOperation projectionOperation = Aggregation.project()
                .and("_id").as("name")
                .andInclude("value")
                .andExclude("_id");

        Aggregation aggregation = Aggregation.newAggregation(
                matchByGroupId,
                projectionOperation
        );

        AggregationResults<GroupTaskCountDto> results = mongoTemplate.aggregate(aggregation, "tasks", GroupTaskCountDto.class);

        log.info("Fetched the group tasks count successfully");
        return results.getMappedResults();
    }

    @Override
    public GroupEditResponse editGroupInfo(String groupId, String name, String description, MultipartFile photoFile) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new NoGroupFoundException("No group associated with the groupId"));

        if (photoFile != null) {
            try {
                String path = "groupPhotos";

                amazonS3Service.updateFileInS3(path, groupId, photoFile.getInputStream());

                group.setPhotoUri(path + "/" + groupId);

                log.info("Group photo updated successfully");
            } catch (IOException e) {
                log.error("Error updating the group photo", e);
                throw new RuntimeException("Failed to update the group photo", e);
            }
        }

        if (name != null && !name.trim().isEmpty()) {
            group.setName(name);
        }

        if (description != null && !description.trim().isEmpty()) {
            group.setDescription(description);
        }

        groupRepository.save(group);

        log.info("Group info successfully edited");
        GroupEditResponse response = new GroupEditResponse();
        response.setPhotoUrl(converterService.convertPhotoUriToUrl(group.getPhotoUri()));
        return response;
    }


    @Override
    public List<GroupDto> getAllUserGroups(String userId, Pageable pageable) {
        log.info("Fetching user memberships");
        List<UserGroupRelationDto> allUsersGroupMemberships = userGroupRelationService.getMembershipsByUserId(userId, pageable);
        List<Group> userGroups = new ArrayList<>();

        for(UserGroupRelationDto userGroupRelation : allUsersGroupMemberships) {
            Group group = groupRepository.findById(userGroupRelation.getGroupId())
                    .orElseThrow(()-> new NoGroupFoundException("There is a relation between user and a group that doesnt exist"));
            userGroups.add(group);
        }

        return userGroups.stream()
                .map(converterService::convertToGroupDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserGroupRelationDto getGroupRoles(String groupId) {
        groupRepository.findById(groupId).orElseThrow(() -> new NoGroupFoundException("No group associated with the groupId"));

        UserGroupRelation membership = groupMembershipRepository.findByUserIdAndGroupId(Helper.getLoggedInUserId(), groupId);

        return converterService.convertToUserGroupRelation(membership);
    }



    // important to get all user profiles that belong to some group...
    @Override
    public List<GroupMemberResponse> getGroupMembers(String groupId, Pageable pageable) {
        groupRepository.findById(groupId).orElseThrow(() -> new NoGroupFoundException("No group associated with the groupId"));
        List<UserGroupRelationDto> userGroupRelationResponses = userGroupRelationService.getMembershipsByGroupId(groupId,pageable);
        List<GroupMemberResponse> groupUsers = new ArrayList<>();
        for (UserGroupRelationDto userGroupRelationResponse : userGroupRelationResponses) {
             User user = userRepository.findById(userGroupRelationResponse
                     .getUserId())
                     .orElseThrow(()->  new NoUserFoundException("Trying to fetch a user that doesn't exist"));

             groupUsers.add(converterService.convertUserToGroupMemberResponse(user, userGroupRelationResponse.getUserRole()));

        }

        return groupUsers;

    }

    @Override
    public void kickUserFromGroup(String groupId, String userProfileId) {
        groupRepository.findById(groupId).orElseThrow(() -> new NoGroupFoundException("No group associated with the groupId"));

        UserGroupRelation membership = groupMembershipRepository.findByUserIdAndGroupId(userProfileId, groupId);
        if (membership == null) {
            throw new NotFoundException("User is not member of this group");
        }


        boolean isTargetUserAdmin = authService.hasRole(groupId, Role.ADMIN);

        if(!isTargetUserAdmin) {
            throw new IllegalStateException("Only the Admin can kick members");
        }

        groupMembershipRepository.delete(membership);
        log.info("User Profile {} kicked from group {}", userProfileId, groupId);
    }

    @Override
    public void promoteUser(ChangeGroupAdminDto changeGroupAdminDto) {
        groupRepository.findById(changeGroupAdminDto.getGroupId()).orElseThrow(
                () -> new NoGroupFoundException("No group associated with the groupId"));

        UserGroupRelation membership = groupMembershipRepository.findByUserIdAndGroupId(changeGroupAdminDto.getUserId(),
                changeGroupAdminDto.getGroupId());

        if (membership == null) {
            throw new NotFoundException("User is not a member of this group");
        }

        if (!membership.getRole().equals(changeGroupAdminDto.getRole())) {
            membership.setRole(changeGroupAdminDto.getRole());
            groupMembershipRepository.save(membership);
            log.info("Role {} assigned to user {} in group {}", changeGroupAdminDto.getRole(), changeGroupAdminDto.getUserId(),
                    changeGroupAdminDto.getGroupId());
        } else {
            log.info("User {} already has role {} in group {}", changeGroupAdminDto.getUserId(),
                    changeGroupAdminDto.getRole(),
                    changeGroupAdminDto.getGroupId());
        }

    }
}
