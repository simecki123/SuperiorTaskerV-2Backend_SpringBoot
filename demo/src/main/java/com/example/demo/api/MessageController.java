package com.example.demo.api;

import com.example.demo.exceptions.NoGroupFoundException;
import com.example.demo.exceptions.NoMessageException;
import com.example.demo.exceptions.NoUserFoundException;
import com.example.demo.models.dao.Message;
import com.example.demo.models.dto.MessageRequest;
import com.example.demo.models.dto.MessageResponse;
import com.example.demo.models.enums.MessageStatus;
import com.example.demo.service.MessageService;
import com.example.demo.config.openapi.ShowAPI;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("api/messages")
@Slf4j
@AllArgsConstructor
@ShowAPI
public class MessageController {
    private final MessageService messageService;

    @PostMapping("/create-message")
    public ResponseEntity<MessageResponse> createMessage(
            @RequestBody MessageRequest message
    ) {
        try {
            log.info("Creating message for some user ");
            return ResponseEntity.ok(messageService.createMessage(message));
        } catch (NoGroupFoundException | NoUserFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Error e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/update-message-status")
    public ResponseEntity<String> updateMessageStatus(
            @RequestParam(required = true) String messageId,
            @RequestParam(required = true) MessageStatus messageStatus
    ) {
        try {
            log.info("Updating message status");
            return ResponseEntity.ok(messageService.editMessage(messageId, messageStatus));

        } catch (NoMessageException e) {
            return ResponseEntity.notFound().build();
        } catch (Error e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/get-messages")
    public ResponseEntity<List<Message>> getMessages(
            @RequestParam(required = false) String userProfileId,
            @RequestParam(required = false) String groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size
    ) {
        log.info("Fetching messages with userProfileId: {} and groupId: {}", userProfileId, groupId);
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<Message> messages = messageService.getAllMessages(userProfileId, groupId, pageable);

            if (messages.isEmpty()) {
                log.info("No messages found for the provided filters.");
                return ResponseEntity.ok(Collections.emptyList());
            }

            log.info("Found {} messages.", messages.size());
            return ResponseEntity.ok(messages);

        } catch (NoGroupFoundException e) {
            log.error("Group not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error fetching messages: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
