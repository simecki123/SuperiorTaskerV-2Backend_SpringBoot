package com.example.demo.security.services.impl;


import com.example.demo.models.dto.UserDto;
import com.example.demo.models.enums.Role;
import com.example.demo.security.api.dto.LoginRequest;
import com.example.demo.security.api.dto.LoginResponse;
import com.example.demo.security.api.dto.RegisterUserRequest;
import com.example.demo.security.api.dto.RegisterUserResponse;
import com.example.demo.security.services.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    @Override
    public UserDto fetchMe() {
        return null;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        return null;
    }

    @Override
    public RegisterUserResponse register(RegisterUserRequest request) {
        return null;
    }

    @Override
    public boolean hasRole(String groupId, Role... requiredRoles) {
        return false;
    }
}
