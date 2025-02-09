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
import com.example.demo.security.utils.Helper;
import com.example.demo.service.AmazonS3Service;
import com.example.demo.service.GroupService;
import com.example.demo.service.UserGroupRelationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final UserGroupRelationRepository groupMembershipRepository;
    private final AmazonS3Service amazonS3Service;
    private final UserGroupRelationService userGroupRelationService;

    /**
     * Method that handles creation of the group.
     * @param name name of the group.
     * @param description description of the group.
     * @param photoFile file of the profile photo of the group.
     * @return new group.
     */
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

        groupRepository.save(group);
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
        // Saving group again because we must save group before creating file S3, and we need to update groupRepository to contain PhotoUri.
        groupRepository.save(group);

        //? Saving the relation of the profile and group.
        UserGroupRelation groupMembership = new UserGroupRelation();
        groupMembership.setGroupId(group.getId());
        groupMembership.setUserId(Helper.getLoggedInUserId());
        groupMembership.setRole(Role.ADMIN);
        groupMembershipRepository.save(groupMembership);


        GroupResponse response = new GroupResponse();
        response.setGroupId(group.getId());
        response.setName(name);
        response.setDescription(description);

        log.info("Group: {}", response);
        log.info("Relation: {}", groupMembership);

        log.info("Group created");
        return response;
    }

    /**
     * Method that handles fetching group by id.
     * @param id group id.
     * @return wanted group.
     */
    @Override
    public GroupDto getGroupById(String id) {
        Group group = groupRepository.findById(id).orElseThrow(() -> new NoGroupFoundException("No group associated with that id"));

        log.info("Get group by id finished");
        return converterService.convertToGroupDto(group);
    }

    /**
     * Method that handles creation of the group.
     * @param groupId id of the group.
     * @param name Name off the group.
     * @param description Description of the group.
     * @param photoFile New profile image of the group.
     * @return updated group.
     */
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
        response.setName(group.getName());
        response.setDescription(group.getDescription());
        response.setPhotoUri(converterService.convertPhotoUriToUrl(group.getPhotoUri()));
        return response;
    }

    /**
     * Get all groups that some user belongs to.
     * @param userId id of the wanted user.
     * @param pageable Pagination.
     * @return list of the groups.
     */
    @Override
    public List<GroupDto> getAllUserGroups(String userId, Pageable pageable) {
        log.info("Fetching user memberships");
        List<UserGroupRelationDto> allUsersGroupMemberships = userGroupRelationService.getMembershipsByUserId(userId, pageable);
        List<Group> userGroups = new ArrayList<>();

        for(UserGroupRelationDto userGroupRelation : allUsersGroupMemberships) {
            Group group = groupRepository.findById(userGroupRelation.getGroupId())
                    .orElseThrow(()-> new NoGroupFoundException("There is a relation between user and a group that doesn't exist"));
            userGroups.add(group);
        }

        return userGroups.stream()
                .map(converterService::convertToGroupDto)
                .collect(Collectors.toList());
    }

    /**
     * Method that returns members of some group.
     * @param groupId id of the wanted group.
     * @param pageable Pagination.
     * @return group members.
     */
    @Override
    public List<GroupMemberResponse> getGroupMembers(String groupId, Pageable pageable) {
        groupRepository.findById(groupId).orElseThrow(() -> new NoGroupFoundException("No group associated with the groupId"));
        User userProfile = userRepository.getUserById(Helper.getLoggedInUserId());
        List<UserGroupRelationDto> userGroupRelationResponses = userGroupRelationService.getMembershipsByGroupId(groupId,pageable);
        List<GroupMemberResponse> groupUsers = new ArrayList<>();
        for (UserGroupRelationDto userGroupRelationResponse : userGroupRelationResponses) {
             User user = userRepository.findById(userGroupRelationResponse
                     .getUserId())
                     .orElseThrow(()->  new NoUserFoundException("Trying to fetch a user that doesn't exist"));
            if(!user.getId().equals(userProfile.getId())) {
                groupUsers.add(converterService.convertUserToGroupMemberResponse(user, userGroupRelationResponse.getUserRole()));
            }

        }

        return groupUsers;

    }

    /**
     * Make user admin of some group.
     * @param changeGroupAdminDto dto that contains some important data for this method to work properly.
     */
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
