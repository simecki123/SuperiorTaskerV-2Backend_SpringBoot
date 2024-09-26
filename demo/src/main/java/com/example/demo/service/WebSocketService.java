package com.example.demo.service;

import com.example.demo.models.dao.Message;

public interface WebSocketService {
    void notifyGroupUsersOfNewMessage(Message message);
}
