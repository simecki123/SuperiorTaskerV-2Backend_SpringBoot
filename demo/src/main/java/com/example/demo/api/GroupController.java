package com.example.demo.api;

import com.example.demo.exceptions.NoGroupFoundException;
import com.example.demo.models.dto.GroupRequest;
import com.example.demo.models.dto.GroupResponse;
import com.example.demo.models.dto.TaskRequest;
import com.example.demo.models.dto.TaskResponse;
import com.example.demo.service.GroupService;
import com.example.demo.service.UserService;
import com.example.demo.config.openapi.ShowAPI;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/groups")
@Slf4j
@AllArgsConstructor
@ShowAPI
public class GroupController {
    private final GroupService groupService;

    @PostMapping("/createGroup")
    public ResponseEntity<GroupResponse> crateGroup(@RequestBody GroupRequest groupRequest) {
        try {
            log.info("Creating new task...");
            return ResponseEntity.ok(groupService.createGroup(groupRequest));
        }catch (NoGroupFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Error e) {
            return ResponseEntity.badRequest().build();
        }

    }
}
