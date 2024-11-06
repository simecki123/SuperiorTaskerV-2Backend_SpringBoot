package com.example.demo.models.dto;

import com.example.demo.models.enums.MessageStatus;
import com.example.demo.service.MessageService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequest {

    @NotBlank
    @Size(max = 50)
    private String groupId;
    @NotBlank
    @Size(max = 50)
    private String userId;

    @NotBlank
    @Size(max = 50)
    private String message;






}
