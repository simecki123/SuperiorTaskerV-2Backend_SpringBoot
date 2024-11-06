package com.example.demo.models.dto;

import com.example.demo.models.enums.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
    private String id;
    private String message;
    private MessageStatus messageStatus;
    private String firstName;
    private String lastName;
    private String photoUri;
}
