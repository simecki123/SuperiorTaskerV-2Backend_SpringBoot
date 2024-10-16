package com.example.demo.api;

import com.example.demo.config.openapi.ShowAPI;
import com.example.demo.exceptions.NoGroupFoundException;
import com.example.demo.exceptions.NoUserFoundException;
import com.example.demo.models.dto.UserGroupRelationRequest;
import com.example.demo.models.dto.UserGroupRelationResponse;
import com.example.demo.service.UserGroupRelationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/user-group-relation")
@Slf4j
@AllArgsConstructor
@ShowAPI
public class UserGroupRelationController {
    private final UserGroupRelationService userGroupRelationService;
    

}
