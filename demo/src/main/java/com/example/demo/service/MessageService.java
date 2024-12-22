package com.example.demo.service;
import com.example.demo.models.dao.Message;
import com.example.demo.models.dto.MessageRequest;
import com.example.demo.models.dto.MessageResponse;
import com.example.demo.models.enums.MessageStatus;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface MessageService {
    MessageResponse createMessage(MessageRequest messageRequest);
    String editMessage(String messageId, MessageStatus messageStatus);
    List<Message> getAllMessages(String userProfileId, String groupId, Pageable pageable);
}
