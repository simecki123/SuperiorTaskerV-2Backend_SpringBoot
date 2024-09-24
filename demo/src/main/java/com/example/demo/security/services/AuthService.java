package com.example.demo.security.services;

import com.example.demo.models.dto.UserDto;
import com.example.demo.models.enums.Role;
import com.example.demo.security.api.dto.LoginRequest;
import com.example.demo.security.api.dto.LoginResponse;
import com.example.demo.security.api.dto.RegisterUserRequest;
import com.example.demo.security.api.dto.RegisterUserResponse;

public interface AuthService {
    UserDto fetchMe();
    LoginResponse login(LoginRequest request);
    RegisterUserResponse register(RegisterUserRequest request);
    //void sendVerificationEmail(User user);
    //void verifyUser(String invitationId, String verificationCode);
    //void changePassword(PasswordChangeRequest request);
    //void requestPasswordReset(PasswordResetRequest request);
    //void resetPassword(PasswordResetRequest request);
    boolean hasRole(String groupId, Role... requiredRoles);


}
