package com.example.demo.api;

import com.example.demo.exceptions.NoGroupFoundException;
import com.example.demo.models.dto.*;
import com.example.demo.service.GroupService;
import com.example.demo.service.UserService;
import com.example.demo.config.openapi.ShowAPI;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

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


    @GetMapping("/get-all-user-groups")
    public ResponseEntity<List<GroupDto>> getAllUserGroups(@RequestParam String userId){
        try {
            log.info("getting all groups for user...");
            return ResponseEntity.ok(groupService.getAllUserGroups(userId)) ;
        }catch (NoGroupFoundException e){
            return ResponseEntity.notFound().build();
        } catch (Error e){
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/get-all-group-members")
    public ResponseEntity<List<GroupMemberResponse>> getAllGroupUsers(
            @RequestParam String groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size)
    {
        log.info("Fetching all users that belong to this group...");
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<GroupMemberResponse> users = groupService.getGroupMembers(groupId, pageable);

            if (users.isEmpty()) {
                log.info("No projects found for userId: {}", groupId);
                return ResponseEntity.ok(Collections.emptyList());
            }
            log.info("Found {} projects", users.size());
            return ResponseEntity.ok(users);


        } catch (NoGroupFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Error e){
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/change-role-for-group-user")
    public ResponseEntity<String> changeGroupAdmin(@RequestBody ChangeGroupAdminDto changeGroupAdminDto){
        try {
            log.info("Changing admin of the group...");
            groupService.promoteUser(changeGroupAdminDto);
            return ResponseEntity.ok("Successfully changed role of the user for this group...");
        } catch (Error e) {
            return ResponseEntity.badRequest().build();
        }
    }







}
