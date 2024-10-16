package com.example.demo.service.impl;

import com.amazonaws.services.kms.model.NotFoundException;
import com.example.demo.converters.ConverterService;
import com.example.demo.exceptions.GroupAlreadyExistsException;
import com.example.demo.exceptions.NoGroupFoundException;
import com.example.demo.models.dao.Group;
import com.example.demo.models.dao.UserGroupRelation;
import com.example.demo.models.dto.*;
import com.example.demo.models.enums.Role;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.UserGroupRelationRepository;
import com.example.demo.security.services.AuthService;
import com.example.demo.security.utils.Helper;
import com.example.demo.service.AmazonS3Service;
import com.example.demo.service.GroupService;
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
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;

    private final ConverterService converterService;

    private final PasswordEncoder passwordEncoder;

    private final MongoTemplate mongoTemplate;

    private final UserGroupRelationRepository groupMembershipRepository;

    private final AmazonS3Service amazonS3Service;

    private final AuthService authService;


    @Override
    public GroupResponse createGroup(GroupRequest request) {
        Optional<Group> groupOptional = groupRepository.findByName(request.getName());

        if(groupOptional.isPresent()) {
            String existingGroupName = groupOptional.get().getName().toLowerCase();
            String groupNameRequest = request.getName().toLowerCase();
            if (groupNameRequest.equals(existingGroupName)) {
                throw new GroupAlreadyExistsException("Group name already exists: " + request.getName());
            }
        }
        Group group = new Group();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        groupRepository.save(group);

        //? Saving the relation of the profile and group
        UserGroupRelation groupMembership = new UserGroupRelation();
        groupMembership.setGroupId(group.getId());
        groupMembership.setUserId(Helper.getLoggedInUserId());
        groupMembership.setRole(Role.ADMIN);
        groupMembershipRepository.save(groupMembership);


        GroupResponse response = new GroupResponse();
        response.setGroupId(group.getId());
        response.setName(request.getName());
        response.setDescription(request.getDescription());

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
                String fileName = groupId;
                String path = "groupPhotos";

                amazonS3Service.updateFileInS3(path, fileName, photoFile.getInputStream());

                group.setPhotoUri(path + "/" + fileName);

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
    public List<GroupDto> getProfileGroups() {
        if (Helper.getLoggedInUserId() == null) {
            throw new IllegalStateException("Bad user profile id present");
        }

        MatchOperation matchOperation = Aggregation.match(Criteria.where("userProfileId").is(Helper.getLoggedInUserId()));

        AddFieldsOperation addFieldsOperation = Aggregation.addFields()
                .addField("groupId")
                .withValueOf(ConvertOperators.ToObjectId.toObjectId("$groupId"))
                .build();


        LookupOperation lookupOperation = Aggregation.lookup("groups", "groupId", "_id", "group");

        UnwindOperation unwindOperation = Aggregation.unwind("group");

        ProjectionOperation projectionOperation = Aggregation.project()
                .andExclude("_id")
                .and("group._id").as("_id")
                .and("group.name").as("name")
                .and("group.description").as("description")
                .and("group.photoUri").as("photoUri");

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                addFieldsOperation,
                lookupOperation,
                unwindOperation,
                projectionOperation
        );

        AggregationResults<Group> results = mongoTemplate.aggregate(aggregation, "UserGroupRelation", Group.class);

        return results.getMappedResults().stream()
                .map(converterService::convertToGroupDto)
                .toList();
    }

    @Override
    public UserGroupRelationDto getGroupRoles(String groupId) {
        groupRepository.findById(groupId).orElseThrow(() -> new NoGroupFoundException("No group associated with the groupId"));

        UserGroupRelation membership = groupMembershipRepository.findByUserIdAndGroupId(Helper.getLoggedInUserId(), groupId);

        return converterService.convertToUserGroupRelation(membership);
    }



    @Override
    public List<GroupMemberResponse> getGroupMembers(String groupId, Pageable pageable) {
        groupRepository.findById(groupId).orElseThrow(() -> new NoGroupFoundException("No group associated with the groupId"));

        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();

        MatchOperation matchOperation = Aggregation.match(Criteria.where("groupId").is(groupId));

        AddFieldsOperation addFieldsOperation = Aggregation.addFields()
                .addField("userId")
                .withValueOf(ConvertOperators.ToObjectId.toObjectId("$userId"))
                .build();

        LookupOperation lookupOperation = Aggregation.lookup("users", "userId", "_id", "user");

        UnwindOperation unwindOperation = Aggregation.unwind("user");

        ProjectionOperation projectionOperation = Aggregation.project()
                .andExclude("_id")
                .andInclude("userId")
                .andInclude("roles")
                .and("user.firstName").as("firstName")
                .and("user.lastName").as("lastName")
                .and("user.photoUri").as("photoUrl");

        SortOperation sortOperation = Aggregation.sort(Sort.by(Sort.Direction.ASC, "firstName", "lastName"));

        SkipOperation skipOperation = Aggregation.skip((long) pageNumber * pageSize);

        LimitOperation limitOperation = Aggregation.limit(pageSize);

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                addFieldsOperation,
                lookupOperation,
                unwindOperation,
                projectionOperation,
                sortOperation,
                skipOperation,
                limitOperation
        );

        AggregationResults<GroupMemberDto> results = mongoTemplate.aggregate(aggregation,
                "groupMemberships", GroupMemberDto.class);


        return results.getMappedResults().stream()
                .map(converterService::convertToGroupMemberResponse)
                .toList();

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
    public void promoteUser(String groupId, String userId, Role role) {
        groupRepository.findById(groupId).orElseThrow(() -> new NoGroupFoundException("No group associated with the groupId"));

        UserGroupRelation membership = groupMembershipRepository.findByUserIdAndGroupId(userId, groupId);
        if (membership == null) {
            throw new NotFoundException("User is not a member of this group");
        }

        if (!membership.getRole().equals(role)) {
            membership.setRole(role);
            groupMembershipRepository.save(membership);
            log.info("Role {} assigned to user {} in group {}", role, userId, groupId);
        } else {
            log.info("User {} already has role {} in group {}", userId, role, groupId);
        }

    }
}
