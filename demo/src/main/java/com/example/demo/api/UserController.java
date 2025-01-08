package com.example.demo.api;

import com.example.demo.exceptions.NoGroupFoundException;
import com.example.demo.models.dto.*;
import com.example.demo.service.UserService;
import com.example.demo.config.openapi.ShowAPI;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("api/users")
@Slf4j
@AllArgsConstructor
@ShowAPI
public class UserController {
    private final UserService userService;

    @PatchMapping("/update-user")
    public ResponseEntity<UserProfileEditResponse> updateUser(
            @RequestPart(value = "firstName", required = false) String firstName,
            @RequestPart(value = "lastName", required = false) String lastName,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        try {
            log.info("Editing group info");
            return ResponseEntity.ok(userService.editUserProfile(firstName,lastName, description, file));

        }  catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        } catch (Error e){
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/add-new-member-to-group")
    public ResponseEntity<List<UserToAddInGroupResponse>> fetchUser(
            @RequestParam(value = "groupId", required = true) String groupId,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size
    ) {
        try {
            log.info("Fetching users not in group: {}", groupId);
            Pageable pageable = PageRequest.of(page, size);
            List<UserToAddInGroupResponse> groupNotMembers = userService.fetchUserByNameAndNotHisGroup(groupId, search, pageable);

            return ResponseEntity.ok(groupNotMembers);
        } catch (Exception e) {
            log.error("Error fetching users: ", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
