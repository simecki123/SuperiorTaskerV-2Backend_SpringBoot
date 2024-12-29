package com.example.demo.api;

import com.example.demo.exceptions.*;
import com.example.demo.models.dto.*;
import com.example.demo.models.enums.Role;
import com.example.demo.security.services.AuthService;
import com.example.demo.service.GroupService;
import com.example.demo.service.UserGroupRelationService;
import com.example.demo.service.UserService;
import com.example.demo.config.openapi.ShowAPI;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("api/groups")
@Slf4j
@AllArgsConstructor
@ShowAPI
public class GroupController {
    private final GroupService groupService;
    private final AuthService authService;
    private final UserGroupRelationService userGroupRelationService;

    @PostMapping("/createGroup")
    public ResponseEntity<GroupResponse> crateGroup(
            @RequestPart(value = "name") String name,
            @RequestPart(value = "description") String description,
            @RequestPart(value = "photoUri") MultipartFile photoFile) {
        try {
            log.info("Creating new task...");
            return ResponseEntity.ok(groupService.createGroup(name, description, photoFile));
        }catch (NoGroupFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Error e) {
            return ResponseEntity.badRequest().build();
        }

    }


    @GetMapping("/get-all-user-groups")
    public ResponseEntity<List<GroupDto>> getAllUserGroups(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size){
        try {
            log.info("getting all groups for user...");
            Pageable pageable = PageRequest.of(page, size);
            return ResponseEntity.ok(groupService.getAllUserGroups(userId, pageable)) ;
        }catch (NoGroupFoundException e){
            return ResponseEntity.notFound().build();
        } catch (Error e){
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/get-group-by-id")
    public ResponseEntity<GroupDto> getGroupById(
            @RequestParam String groupId
    ) {
        try{
            log.info("getting group by its id");
            return ResponseEntity.ok(groupService.getGroupById(groupId));
        } catch (NoGroupFoundException e) {
            return ResponseEntity.notFound().build();
        }catch(Error e) {
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


    @PatchMapping(value="/editGroup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GroupEditResponse> updateGroup(
            @RequestParam(value = "groupId") String groupId,
            @RequestPart(value = "name", required = false) String name,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {

        if (!authService.hasRole(groupId, Role.ADMIN)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            log.info("Editing group info");
            return ResponseEntity.ok(groupService.editGroupInfo(groupId,name, description,file));

        }  catch (NoGroupFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Error e){
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/add-new-user-to-group")
    public ResponseEntity<UserGroupRelationResponse> addNewUserToGroup(
            @RequestParam String userId,
            @RequestParam String groupId
    ) {
        if (!authService.hasRole(groupId, Role.ADMIN)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            log.info("Create new user group Relation...");
            return ResponseEntity.ok(userGroupRelationService.createNewUserGroupRelation(userId, groupId));
        } catch (NoGroupFoundException | NoUserFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Error | UserGroupRelationAlreadyExistsException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/leave-group")
    public ResponseEntity<String> leaveGroup(
            @RequestParam String userId,
            @RequestParam String groupId
    ) {
        if (authService.hasRole(groupId, Role.ADMIN)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            log.info("Create new user group Relation...");
            return ResponseEntity.ok(userGroupRelationService.leaveGroup(userId, groupId));
        } catch (NoGroupFoundException | NoUserFoundException | NoUserGroupRelation e) {
            return ResponseEntity.notFound().build();
        } catch (Error e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/kick-from-group")
    public ResponseEntity<String> kickUserFromGroup(
            @RequestParam String userId,
            @RequestParam String groupId
    ) {
        if (!authService.hasRole(groupId, Role.ADMIN)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();


        try {
            log.info("Kicking user from the group...");
            return ResponseEntity.ok(userGroupRelationService.kickUser(userId, groupId));
        } catch (NoGroupFoundException | NoUserFoundException | NoUserGroupRelation | CantKickYourselfException e) {
            return ResponseEntity.notFound().build();
        } catch (Error e) {
            return ResponseEntity.badRequest().build();
        }
    }




}
