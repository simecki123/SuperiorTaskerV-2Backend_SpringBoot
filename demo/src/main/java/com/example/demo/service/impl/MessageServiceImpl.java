package com.example.demo.service.impl;

import com.example.demo.converters.ConverterService;
import com.example.demo.exceptions.NoGroupFoundException;
import com.example.demo.models.dao.User;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.utils.Helper;
import com.example.demo.service.MessageService;
import com.example.demo.models.dao.Message;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final UserRepository userProfileRepository;

    private final MongoTemplate mongoTemplate;

    private final ConverterService converterService;

    private final GroupRepository groupRepository;

    @Override
    public List<Message> getAllMessages(String groupId, Pageable pageable) {
        groupRepository.findById(groupId).orElseThrow(() -> new NoGroupFoundException("No group associated with the groupId"));
        User user = userProfileRepository.getUserById(Helper.getLoggedInUserId());
        List<Message> messages = new ArrayList<>();

        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();

        MatchOperation matchOperation = Aggregation.match(
                new Criteria().orOperator(
                        Criteria.where("notificationType").is("ORDER")
                                .and("recipientUserProfileId").is(user.getId()),
                        Criteria.where("notificationType").is("EVENT")
                                .and("userProfileId").ne(user.getId())
                )
        );

        ProjectionOperation projectOperation = Aggregation.project()
                .andInclude("notificationType", "orderId", "userId", "firstName", "lastName",
                        "eventId", "additionalOptions", "createdAt", "photoUri", "groupId",
                        "title", "description")
                .and("photoUri").as("profilePhoto")
                .andExclude("_id");

        SkipOperation skipOperation = Aggregation.skip((long) pageNumber * pageSize);
        LimitOperation limitOperation = Aggregation.limit(pageSize);

        SortOperation sortOperation = Aggregation.sort(Sort.by(Sort.Direction.DESC, "createdAt"));

        Aggregation aggregation  = Aggregation.newAggregation(
                matchOperation,
                projectOperation,
                sortOperation,
                skipOperation,
                limitOperation
        );

        AggregationResults<Message> results = mongoTemplate.aggregate(aggregation , "messages", Message.class);
        messages.addAll(results.getMappedResults());

        messages.forEach(message -> {
            if (message.getPhotoUri() != null) {
                String convertedUrl = converterService.convertPhotoUriToUrl(message.getPhotoUri());
                message.setPhotoUri(convertedUrl);
            }
        });
        return messages;


    }
}
