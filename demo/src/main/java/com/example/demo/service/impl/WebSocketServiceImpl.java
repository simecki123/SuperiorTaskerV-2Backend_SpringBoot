package com.example.demo.service.impl;

import com.example.demo.converters.ConverterService;
import com.example.demo.models.dao.UserGroupRelation;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserGroupRelationRepository;
import com.example.demo.service.WebSocketService;
import com.example.demo.models.dao.Message;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    private final MessageRepository notificationRepository;

    private final UserGroupRelationRepository groupMembershipRepository;

    @Override
    public void notifyGroupUsersOfNewMessage(Message message) {
        notificationRepository.save(message);
        List<UserGroupRelation> memberships = groupMembershipRepository.findAllByGroupId(message.getGroupId());
        log.info("Notifying {} members of the group with a new message", memberships.size());
        for (UserGroupRelation membership : memberships) {
            String userId = membership.getUserId();
            messagingTemplate.convertAndSend("/topic/messages" , message);
        }
    }
}
