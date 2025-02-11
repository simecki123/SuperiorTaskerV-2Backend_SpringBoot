package com.example.demo.api;

import com.example.demo.config.openapi.ShowAPI;
import com.example.demo.service.UserGroupRelationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
