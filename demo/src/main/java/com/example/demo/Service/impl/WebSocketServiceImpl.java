package com.example.demo.Service.impl;

import com.example.demo.Service.WebSocketService;
import com.example.demo.models.dao.Message;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {

    @Override
    public void notifyGroupUsersOfNewMessage(Message message) {

    }
}
