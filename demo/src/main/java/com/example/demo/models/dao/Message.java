package com.example.demo.models.dao;

import com.example.demo.models.enums.MessageStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "messages")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    @Id
    private String id;

    @NotBlank
    @Size(max = 50)
    private String groupId;

    @NotBlank
    @Size(max = 200)
    private String message;
    @NotBlank
    private MessageStatus messageStatus;


    //User who made message
    private String userProfileId;
    private String firstName;
    private String lastName;
    private String photoUri;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
