package com.example.demo.service.impl;

import com.example.demo.models.dao.UserGroupRelation;
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
    private final UserGroupRelationRepository groupMembershipRepository;

    @Override
    public void notifyGroupUsersOfNewMessage(Message message) {
        List<UserGroupRelation> memberships = groupMembershipRepository.findAllByGroupId(message.getGroupId())
                .stream()
                .filter(membership -> !membership.getUserId().equals(message.getUserProfileId()))
                .toList();

        log.info("Notifying {} members of the group with a new message", memberships.size());

        for (UserGroupRelation membership : memberships) {
            String userId = membership.getUserId();
            messagingTemplate.convertAndSend("/topic/messages." + userId, message);
        }
    }
}
