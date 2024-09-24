package com.example.demo.Service;

import com.example.demo.models.dao.Message;

public interface WebSocketService {
    void notifyGroupUsersOfNewMessage(Message message);
}
