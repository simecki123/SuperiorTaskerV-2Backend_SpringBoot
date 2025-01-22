package com.example.demo.service.impl;

import com.example.demo.converters.ConverterService;
import com.example.demo.exceptions.NoGroupFoundException;
import com.example.demo.exceptions.NoMessageException;
import com.example.demo.exceptions.NoUserFoundException;
import com.example.demo.models.dao.User;
import com.example.demo.models.dao.UserGroupRelation;
import com.example.demo.models.dto.MessageRequest;
import com.example.demo.models.dto.MessageResponse;
import com.example.demo.models.enums.MessageStatus;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserGroupRelationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.utils.Helper;
import com.example.demo.service.MessageService;
import com.example.demo.models.dao.Message;
import com.example.demo.service.WebSocketService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;

    private final UserRepository userProfileRepository;

    private final MongoTemplate mongoTemplate;

    private final ConverterService converterService;

    private final GroupRepository groupRepository;

    private final WebSocketService webSocketService;

    private final UserGroupRelationRepository userGroupRelationRepository;

    @Override
    public MessageResponse createMessage(MessageRequest messageRequest) {
        groupRepository.findById(messageRequest.getGroupId()).orElseThrow(() -> new NoGroupFoundException("No group associated with the groupId"));
        User user = userProfileRepository.findById(messageRequest.getUserId()).orElseThrow(() -> new NoUserFoundException("No user associated with user Id"));

        Message message = new Message();
        message.setGroupId(messageRequest.getGroupId());
        message.setUserProfileId(messageRequest.getUserId());
        message.setMessage(messageRequest.getMessage());
        message.setMessageStatus(MessageStatus.UNREAD);
        message.setFirstName(user.getFirstName());
        message.setLastName(user.getLastName());
        message.setPhotoUri(user.getPhotoUri());

        Message savedMessage = messageRepository.save(message);
        log.info("Message created ...");

        webSocketService.notifyGroupUsersOfNewMessage(savedMessage);

        return converterService.convertToMessageResponse(message);
    }

    @Override
    public String editMessage(String messageId, MessageStatus messageStatus) {
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new NoMessageException("There is no message with associated id"));
        message.setMessageStatus(messageStatus);
        messageRepository.save(message);
        return "Message updated";

    }

    @Override
    public List<Message> getAllMessages(String userProfileId, String groupId, Pageable pageable) {
        Query query = new Query();

        if (groupId != null) {
            groupRepository.findById(groupId).orElseThrow(() -> new NoGroupFoundException("No group associated with the groupId"));
            query.addCriteria(Criteria.where("groupId").is(groupId));
        } else if (userProfileId != null) {
            List<UserGroupRelation> userGroups = userGroupRelationRepository.findAllByUserId(userProfileId);
            List<String> groupIds = userGroups.stream()
                    .map(UserGroupRelation::getGroupId)
                    .collect(Collectors.toList());

            if (!groupIds.isEmpty()) {
                query.addCriteria(Criteria.where("groupId").in(groupIds));
            }
        }

        query.with(Sort.by(Sort.Direction.DESC, "createdAt"))
                .skip((long) pageable.getPageNumber() * pageable.getPageSize())
                .limit(pageable.getPageSize());

        List<Message> messages = mongoTemplate.find(query, Message.class);

        messages.forEach(message -> {
            if (message.getPhotoUri() != null) {
                String convertedUrl = converterService.convertPhotoUriToUrl(message.getPhotoUri());
                message.setPhotoUri(convertedUrl);
            }
        });

        return messages;
    }

}
