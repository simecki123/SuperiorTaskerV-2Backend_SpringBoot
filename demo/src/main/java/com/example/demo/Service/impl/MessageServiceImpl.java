package com.example.demo.Service.impl;

import com.example.demo.Service.MessageService;
import com.example.demo.models.dao.Message;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class MessageServiceImpl implements MessageService {
    @Override
    public List<Message> getAllMessages(String groupId, Pageable pageable) {
        return null;
    }
}
