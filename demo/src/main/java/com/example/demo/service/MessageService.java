package com.example.demo.service;
import com.example.demo.models.dao.Message;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface MessageService {
    List<Message> getAllMessages(String groupId, Pageable pageable);
}
