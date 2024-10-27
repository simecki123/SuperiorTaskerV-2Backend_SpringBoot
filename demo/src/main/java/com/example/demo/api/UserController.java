package com.example.demo.api;

import com.example.demo.exceptions.NoGroupFoundException;
import com.example.demo.models.dto.UserDto;
import com.example.demo.models.dto.UserProfileEditResponse;
import com.example.demo.service.UserService;
import com.example.demo.config.openapi.ShowAPI;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
}
