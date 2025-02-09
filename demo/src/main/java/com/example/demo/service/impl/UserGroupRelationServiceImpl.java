package com.example.demo.service.impl;

import com.example.demo.converters.ConverterService;
import com.example.demo.exceptions.*;
import com.example.demo.models.dao.Task;
import com.example.demo.models.dao.UserGroupRelation;
import com.example.demo.models.dto.*;

import com.example.demo.models.enums.Role;
import com.example.demo.repository.*;
import com.example.demo.security.services.AuthService;
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


    /**
     * Method to get user group relations by User id (Memberships).
     * @param userId Wanted user.
     * @param pageable Pagination.
     * @return User group relation returns user and group id and user role for that group.
     */
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

    /**
     * get All user memberships for certain group.
     * @param groupId wanted group.
     * @param pageable Pagination.
     * @return User group relation returns user and group id and user role for that group.
     */
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

    /**
     * Method for creating new user group relation (Membership)
     * @param userId User
     * @param groupId Group
     * @return Returns new User group Relation
     * @throws UserGroupRelationAlreadyExistsException user is already member of this group.
     */
    @Override
    public UserGroupRelationResponse createNewUserGroupRelation(String userId, String groupId) throws UserGroupRelationAlreadyExistsException {
        groupRepository.findById(groupId).orElseThrow(() -> new NoGroupFoundException("No group associated with the groupId"));
        userRepository.findById(userId).orElseThrow(() -> new NoUserFoundException("User doesn't exist..."));
        UserGroupRelation alreadyPresentUserGroupRelation =userGroupRelationRepository.findByUserIdAndGroupId(userId, groupId);
        if(alreadyPresentUserGroupRelation != null) {
            throw new UserGroupRelationAlreadyExistsException("This user is already a member of this group");
        }
        log.info("User group relation is under creation process...");

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

        log.info("New user group relation is successfully created...");

        return userGroupRelationResponse;

    }

    /**
     * Create multiple user group relations at once. On frontend its limited to 5 per try.
     * @param users Users that will be new members of certain group.
     * @param groupId Group.
     * @return Return list of user group relations that were created.
     */
    @Override
    public List<UserGroupRelationResponse> createMultipleUserGroupRelations(List<UserToAddInGroupResponse> users, String groupId) {
        log.info("Searching for a valid group using id that was given");
        groupRepository.findById(groupId)
                .orElseThrow(() -> new NoGroupFoundException("No group associated with the groupId"));

        log.info("Group found...");
        List<UserGroupRelationResponse> responses = new ArrayList<>();

        for (UserToAddInGroupResponse user : users) {
            log.info("Verifying that user we want to add exists");
            userRepository.findById(user.getUserId())
                    .orElseThrow(() -> new NoUserFoundException("User doesn't exist..."));

            log.info("Checking if user is already part of this group");
            UserGroupRelation existingRelation =
                    userGroupRelationRepository.findByUserIdAndGroupId(user.getUserId(), groupId);

            if (existingRelation != null) {
                log.error("User is already part of that group");
                // we won't throw exception but just continue with iteration and skip this user that already has membership.
                // This  case will never happen in application because it's already handled on the frontend.
                continue;
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

    /**
     * Method to remove user from the group.
     * @param userId User we want to remove.
     * @param groupId Group we want to remove user from.
     * @param isKick Is admin kicking user out or is user leaving group on his own.
     * @return Message if user is kicked or left.
     * @throws NoUserGroupRelation User can't be removed from the group that he is not part of.
     * @throws CantKickYourselfException Admin cant kick himself (Won't happen, not possible on frontend).
     * @throws UserNotAdminException Logged-in user is not admin, so he can't kick user from the group.
     */
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

        // THIS IS IMPORTANT - all tasks that user that is kicked used to have, are now assigned to admin that kicked him out.
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
