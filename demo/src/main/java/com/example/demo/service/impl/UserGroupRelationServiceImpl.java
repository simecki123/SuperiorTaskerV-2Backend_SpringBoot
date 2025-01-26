package com.example.demo.service.impl;

import com.example.demo.converters.ConverterService;
import com.example.demo.exceptions.*;
import com.example.demo.models.dao.Task;
import com.example.demo.models.dao.User;
import com.example.demo.models.dao.UserGroupRelation;
import com.example.demo.models.dto.*;

import com.example.demo.models.enums.Role;
import com.example.demo.repository.*;
import com.example.demo.security.services.AuthService;
import com.example.demo.security.utils.Helper;
import com.example.demo.service.UserGroupRelationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class UserGroupRelationServiceImpl implements UserGroupRelationService {

   private final UserRepository userRepository;
   private final GroupRepository groupRepository;
   private final UserGroupRelationRepository userGroupRelationRepository;
    private final ConverterService converterService;
    private final MongoTemplate mongoTemplate;
    private final AuthService authService;
    private final TaskRepository taskRepository;


    @Override
    public List<UserGroupRelationDto> getMembershipsByUserId(String userId, Pageable pageable) {
        log.info("Fetching all user group relations for this group...");
        Criteria criteria = new Criteria();
        criteria.and("userId").is(userId);

        MatchOperation matchOperation = Aggregation.match(criteria);

        ProjectionOperation userGroupRelationOperation = Aggregation.project()
                .and("id").as("id")
                .and("userId").as("userId")
                .and("groupId").as("groupId")
                .and("role").as("role");

        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                userGroupRelationOperation,
                skipOperation,
                limitOperation
        );

        AggregationResults<UserGroupRelation> results = mongoTemplate
                .aggregate(aggregation, "user-group-relation", UserGroupRelation.class);
        log.info("Found {} results", results.getMappedResults().size());


        return results.getMappedResults()
                .stream()
                .map(converterService::convertToUserGroupRelation)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserGroupRelationDto> getMembershipsByGroupId(String groupId, Pageable pageable) {
        log.info("Fetching all user group relations for this group...");
        Criteria criteria = new Criteria();
        criteria.and("groupId").is(groupId);

        MatchOperation matchOperation = Aggregation.match(criteria);

        ProjectionOperation userGroupRelationOperation = Aggregation.project()
                .and("id").as("id")
                .and("userId").as("userId")
                .and("groupId").as("groupId")
                .and("role").as("role");

        SkipOperation skipOperation = Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize());
        LimitOperation limitOperation = Aggregation.limit(pageable.getPageSize());

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                userGroupRelationOperation,
                skipOperation,
                limitOperation
        );

        AggregationResults<UserGroupRelation> results = mongoTemplate
                .aggregate(aggregation, "user-group-relation", UserGroupRelation.class);
        log.info("Found {} results", results.getMappedResults().size());


        return results.getMappedResults()
                .stream()
                .map(converterService::convertToUserGroupRelation)
                .collect(Collectors.toList());
    }

    @Override
    public UserGroupRelationResponse createNewUserGroupRelation(String userId, String groupId) throws UserGroupRelationAlreadyExistsException {
        groupRepository.findById(groupId).orElseThrow(() -> new NoGroupFoundException("No group associated with the groupId"));
        userRepository.findById(userId).orElseThrow(() -> new NoUserFoundException("User doesn't exist..."));
        UserGroupRelation alreadyPresentUserGroupRelation =userGroupRelationRepository.findByUserIdAndGroupId(userId, groupId);
        if(alreadyPresentUserGroupRelation != null) {
            throw new UserGroupRelationAlreadyExistsException("This user is already a member of this group");
        }

        UserGroupRelation newUserGroupRelation = new UserGroupRelation();
        newUserGroupRelation.setGroupId(groupId);
        newUserGroupRelation.setUserId(userId);
        newUserGroupRelation.setRole(Role.USER);

        userGroupRelationRepository.save(newUserGroupRelation);

        UserGroupRelationResponse userGroupRelationResponse = new UserGroupRelationResponse();
        userGroupRelationResponse.setId(newUserGroupRelation.getId());
        userGroupRelationResponse.setGroupId(newUserGroupRelation.getGroupId());
        userGroupRelationResponse.setGroupId(newUserGroupRelation.getGroupId());
        userGroupRelationResponse.setRole(newUserGroupRelation.getRole());

        return userGroupRelationResponse;

    }

    @Override
    public List<UserGroupRelationResponse> createMultipleUserGroupRelations(List<UserToAddInGroupResponse> users, String groupId) throws UserGroupRelationAlreadyExistsException {
        log.info("Searching for a valid group using id taht was given");
        groupRepository.findById(groupId)
                .orElseThrow(() -> new NoGroupFoundException("No group associated with the groupId"));

        log.info("Group found...");
        List<UserGroupRelationResponse> responses = new ArrayList<>();

        for (UserToAddInGroupResponse user : users) {
            log.info("Veryfing taht user we want to add exists");
            userRepository.findById(user.getUserId())
                    .orElseThrow(() -> new NoUserFoundException("User doesn't exist..."));

            log.info("Checking if user is already part of this group");
            UserGroupRelation existingRelation =
                    userGroupRelationRepository.findByUserIdAndGroupId(user.getUserId(), groupId);

            if (existingRelation != null) {
                log.info("User is already part of that group");
                throw new UserGroupRelationAlreadyExistsException(
                        "User " + user.getUserId() + " is already a member of this group"
                );
            }
            log.info("User is not a part of this group lets make him...");
            UserGroupRelation newRelation = new UserGroupRelation();
            newRelation.setGroupId(groupId);
            newRelation.setUserId(user.getUserId());
            newRelation.setRole(Role.USER);

            userGroupRelationRepository.save(newRelation);

            UserGroupRelationResponse response = new UserGroupRelationResponse();
            response.setId(newRelation.getId());
            response.setGroupId(newRelation.getGroupId());
            response.setUserId(newRelation.getUserId());
            response.setRole(newRelation.getRole());

            responses.add(response);
        }

        return responses;
    }

    @Override
    public String removeUserFromGroup(String userId, String groupId, boolean isKick) throws NoUserGroupRelation, CantKickYourselfException, UserNotAdminException {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new NoGroupFoundException("No group associated with the groupId"));
        userRepository.findById(userId)
                .orElseThrow(() -> new NoUserFoundException("User doesn't exist"));

        UserDto loggedInUser = authService.fetchMe();
        UserGroupRelation targetUserRelation = userGroupRelationRepository.findByUserIdAndGroupId(userId, groupId);
        UserGroupRelation loggedInUserRelation = userGroupRelationRepository.findByUserIdAndGroupId(loggedInUser.getId(), groupId);

        if (targetUserRelation == null) {
            throw new NoUserGroupRelation("User is not a member of this group");
        }

        // Check if target user is admin
        if (targetUserRelation.getRole() == Role.ADMIN) {
            throw new CantKickYourselfException("Admin users cannot be removed from the group");
        }

        // For kick operation, verify logged-in user is admin
        if (isKick && ( loggedInUserRelation == null || loggedInUserRelation.getRole() != Role.ADMIN)) {
            throw new UserNotAdminException("Only admins can kick users");
        }

        // Reassign tasks to the admin/logged-in user
        List<Task> userTasks = taskRepository.findAllByUserIdAndGroupId(userId, groupId);
        String newUserId = isKick ? loggedInUser.getId() : loggedInUserRelation.getRole() == Role.ADMIN ? loggedInUser.getId() : null;

        if (newUserId != null) {
            userTasks.forEach(task -> {
                task.setUserId(newUserId);
                taskRepository.save(task);
            });
        }

        userGroupRelationRepository.delete(targetUserRelation);
        return isKick ? "User kicked from group successfully" : "User left group successfully";
    }


}
