package com.example.demo.service.impl;

import com.example.demo.exceptions.NoGroupFoundException;
import com.example.demo.models.dao.User;
import com.example.demo.models.dao.UserGroupRelation;
import com.example.demo.models.dto.*;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.UserGroupRelationRepository;
import com.example.demo.security.utils.Helper;
import com.example.demo.service.AmazonS3Service;
import com.example.demo.service.UserService;
import com.example.demo.converters.ConverterService;
import com.example.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ConverterService converterService;
    private final AmazonS3Service amazonS3Service;
    private final MongoTemplate mongoTemplate;
    private final UserGroupRelationRepository groupMembershipRepository;
    private final GroupRepository groupRepository;

    /**
     * This is service method so user can edit his user-profile
     * @param firstName Text for the first name
     * @param lastName Text for last name
     * @param description Text for description
     * @param photoFile Multipart file. User can send image file
     * @return Return statement returns User Profile response with first name, last name, description and the photo file
     */
    @Override
    public UserProfileEditResponse editUserProfile(String firstName, String lastName, String description, MultipartFile photoFile) {
        User userProfile = userRepository.getUserById(Helper.getLoggedInUserId());
        UserProfileEditResponse response = new UserProfileEditResponse();

        // User profile needs to exist to be edited...
        if (userProfile == null) {
            throw new IllegalStateException("UserProfile is null");
        }

        // if photo file exists then update file in S3 amazon service...
        if (photoFile != null) {
            try {
                String fileName = userProfile.getId();
                String path = "profilePhotos";

                amazonS3Service.updateFileInS3(path, fileName, photoFile.getInputStream());

                userProfile.setPhotoUri(path + "/" + fileName);

                log.info("UserProfile photo updated successfully");
            } catch (IOException e) {
                log.error("Error updating the profile photo", e);
                throw new RuntimeException("Failed to update the UserProfile photo", e);
            }
        }

        if (firstName != null && !firstName.trim().isEmpty()) {
            userProfile.setFirstName(firstName);
        }

        if (lastName != null && !lastName.trim().isEmpty()) {
            userProfile.setLastName(lastName);
        }

        if(description != null && !description.trim().isEmpty()) {
            userProfile.setDescription(description);
        }

        userRepository.save(userProfile);

        log.info("UserProfile successfully updated");

        String url = converterService.convertPhotoUriToUrl(userProfile.getPhotoUri());
        response.setFirstName(userProfile.getFirstName());
        response.setLastName(userProfile.getLastName());
        response.setDescription(userProfile.getDescription());
        response.setProfileUri(url);
        return response;
    }

    /**
     *  Helper method to check if certain group exists...
     * @param groupId ID of wanted group...
     */
    private void validateGroup(String groupId) {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new NoGroupFoundException("No group found with ID: " + groupId));
    }

    /**
     * Helper method to create search criteria. This search criteria is to find user by first name and last name.
     * @return search criteria first and the last name.
     */
    private Criteria buildNameSearchCriteria(String search) {
        Criteria searchCriteria = new Criteria();
        if (search != null && !search.isEmpty()) {
            String[] searchTerms = search.trim().split("\\s+");

            if (searchTerms.length == 1) {
                searchCriteria.orOperator(
                        Criteria.where("firstName").regex(searchTerms[0], "i"),
                        Criteria.where("lastName").regex(searchTerms[0], "i")
                );
            } else {
                List<Criteria> possibleMatches = new ArrayList<>();

                possibleMatches.add(
                        new Criteria().andOperator(
                                Criteria.where("firstName").regex(searchTerms[0], "i"),
                                Criteria.where("lastName").regex(searchTerms[searchTerms.length - 1], "i")
                        )
                );

                possibleMatches.add(
                        new Criteria().andOperator(
                                Criteria.where("lastName").regex(searchTerms[0], "i"),
                                Criteria.where("firstName").regex(searchTerms[searchTerms.length - 1], "i")
                        )
                );

                for (String term : searchTerms) {
                    possibleMatches.add(
                            new Criteria().orOperator(
                                    Criteria.where("firstName").regex(term, "i"),
                                    Criteria.where("lastName").regex(term, "i")
                            )
                    );
                }

                searchCriteria.orOperator(possibleMatches.toArray(new Criteria[0]));
            }
        }
        return searchCriteria;
    }

    /**
     * Helper method to build user aggregation...
     * @param finalCriteria criteria
     * @param pageable for pagination...
     * @return returns wanted aggregation to find users.
     */
    private Aggregation buildUserAggregation(Criteria finalCriteria, Pageable pageable) {
        return Aggregation.newAggregation(
                Aggregation.match(finalCriteria),
                Aggregation.project()
                        .and("_id").as("id")
                        .and("firstName").as("firstName")
                        .and("lastName").as("lastName")
                        .and("email").as("email")
                        .and("photoUri").as("photoUri")
                        .and("description").as("description"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "createdAt")),
                Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                Aggregation.limit(pageable.getPageSize())
        );
    }

    /**
     * Helper method to execute user aggregation and return wanted list of users.
     * @param aggregation all conditions that are required of user.
     * @return list of special user responses...
     */
    private List<UserToAddInGroupResponse> executeUserAggregation(Aggregation aggregation) {
        AggregationResults<User> results = mongoTemplate.aggregate(
                aggregation,
                "users",
                User.class
        );

        return results.getMappedResults()
                .stream()
                .map(converterService::convertUserToUserToAddInGroupResponse)
                .collect(Collectors.toList());
    }

    /**
     * Method to fetch all users from database that are not part of certain group and name
     * @param groupId User shouldn't belong to this group.
     * @param search Users first and last name.
     * @param pageable Pagination
     * @return list of users...
     */
    @Override
    public List<UserToAddInGroupResponse> fetchUserByNameAndNotHisGroup(String groupId, String search, Pageable pageable) {
        validateGroup(groupId);

        Criteria searchCriteria = buildNameSearchCriteria(search);

        List<String> existingUserIds = groupMembershipRepository
                .findAllByGroupId(groupId)
                .stream()
                .map(UserGroupRelation::getUserId)
                .collect(Collectors.toList());

        Criteria notInGroupCriteria = Criteria.where("_id").nin(existingUserIds);
        Criteria finalCriteria = new Criteria().andOperator(searchCriteria, notInGroupCriteria);

        Aggregation aggregation = buildUserAggregation(finalCriteria, pageable);
        return executeUserAggregation(aggregation);
    }

    /**
     * Method that will return list of all users from database that belong to some group.
     * @param groupId Group that user belongs to.
     * @param search Users first and last name.
     * @param pageable Pagination.
     * @return list of users. (Uses same UserToAddInGroupResponse like method above but logic of methods is different. Do not be confused...)
     */
    @Override
    public List<UserToAddInGroupResponse> fetchUsersOfTheGroupWithText(String groupId, String search, Pageable pageable) {
        validateGroup(groupId);

        Criteria searchCriteria = buildNameSearchCriteria(search);

        List<String> groupUserIds = groupMembershipRepository
                .findAllByGroupId(groupId)
                .stream()
                .map(UserGroupRelation::getUserId)
                .collect(Collectors.toList());

        Criteria inGroupCriteria = Criteria.where("_id").in(groupUserIds);
        Criteria finalCriteria = new Criteria().andOperator(searchCriteria, inGroupCriteria);

        Aggregation aggregation = buildUserAggregation(finalCriteria, pageable);
        return executeUserAggregation(aggregation);
    }
}