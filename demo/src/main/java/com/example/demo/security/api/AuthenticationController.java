package com.example.demo.security.api;

import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.exceptions.UnverifiedUserException;
import com.example.demo.exceptions.UserAlreadyExistsException;
import com.example.demo.models.dto.UserDto;
import com.example.demo.security.api.dto.LoginRequest;
import com.example.demo.security.api.dto.LoginResponse;
import com.example.demo.security.api.dto.RegisterUserRequest;
import com.example.demo.security.api.dto.RegisterUserResponse;
import com.example.demo.security.services.AuthService;
import com.example.demo.security.utils.JwtUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("api/auth")
public class AuthenticationController {
    private final AuthService authService;

    private final JwtUtils jwtUtils;

    @PostMapping("register")
    public ResponseEntity<RegisterUserResponse> register(@RequestBody RegisterUserRequest request) {
        try {
            log.info("Register user started");
            return ResponseEntity.ok(authService.register(request));
        } catch (UserAlreadyExistsException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            log.info("Log in started");
            LoginResponse response = authService.login(request);
            ResponseCookie cookie = jwtUtils.createJwtCookie(response.getAccessToken());
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(response);
        } catch (UnauthorizedException | BadCredentialsException e) {
            log.error(String.format("Exception on user authentication: %s", e.getMessage()));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (UnverifiedUserException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("fetchMe")
    public ResponseEntity<UserDto> fetchMe() {
        try {
            log.info("Fetch me started.");
            return ResponseEntity.ok(authService.fetchMe());
        } catch (Exception e) {
            log.error("Error on fetch me: {}!", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
