package com.example.demo.service.impl;

import com.example.demo.converters.ConverterService;
import com.example.demo.exceptions.NoGroupFoundException;
import com.example.demo.exceptions.NoUserFoundException;
import com.example.demo.models.dao.Task;
import com.example.demo.models.dao.User;
import com.example.demo.models.dao.UserGroupRelation;
import com.example.demo.models.dto.UserGroupRelationDto;
import com.example.demo.models.dto.UserGroupRelationRequest;
import com.example.demo.models.dto.UserGroupRelationResponse;

import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.UserGroupRelationRepository;
import com.example.demo.repository.UserRepository;
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
}
