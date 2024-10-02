package com.example.demo.security.services.impl;


import com.example.demo.converters.ConverterService;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.exceptions.UserAlreadyExistsException;
import com.example.demo.models.dao.User;
import com.example.demo.models.dao.UserGroupRelation;
import com.example.demo.models.dto.UserDto;
import com.example.demo.models.enums.Role;
import com.example.demo.repository.UserGroupRelationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.api.dto.LoginRequest;
import com.example.demo.security.api.dto.LoginResponse;
import com.example.demo.security.api.dto.RegisterUserRequest;
import com.example.demo.security.api.dto.RegisterUserResponse;
import com.example.demo.security.services.AuthService;
import com.example.demo.security.utils.JwtUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;

    private final UserGroupRelationRepository userGroupRelationRepository;
    private final JwtUtils jwtUtils;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConverterService converterService;

    @Override
    public UserDto fetchMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDto userDto = new UserDto();

        if (authentication != null && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            userRepository.findByEmail(userDetails.getEmail())
                    .orElseThrow(() -> new IllegalStateException("User not found"));

            List<UserGroupRelation> userGroupRelationList = userGroupRelationRepository.findAllByUserId(userDetails.getId());

            userDto.setFirstName(userDetails.getFirstName());
            userDto.setLastName(userDetails.getLastName());
            userDto.setGroupMembershipData(userGroupRelationList);
            userDto.setProfileUri(userDto.getProfileUri());
            userDto.setEmail(((UserDetailsImpl) authentication.getPrincipal()).getEmail());


            log.info("User successfully \"{}\" fetched.", userDto);
            return userDto;
        } else {
            log.error("User not found in security context");
            throw new IllegalStateException("User not found in security context");
        }


    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
        ));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        log.info("User from from user repo: {}", user);

        LoginResponse response = new LoginResponse();

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtUtils.generateJwtToken(authentication);

        response.setAccessToken(token);

        log.info("Success login with: \"{}\"", request.getEmail());
        return response;
    }

    @Override
    public RegisterUserResponse register(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail()) || request.getEmail() == null) {
            throw new UserAlreadyExistsException("Email is already taken or it is not entered in correct format!");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        userRepository.save(user);

        RegisterUserResponse response = new RegisterUserResponse();
        response.setUserId(user.getId());

        return response;

    }

    @Override
    public boolean hasRole(String groupId, Role... requiredRoles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {

            UserGroupRelation groupMembership = userGroupRelationRepository.findByUserIdAndGroupId(((UserDetailsImpl) authentication.getPrincipal()).getId(), groupId);
            if (groupMembership == null) {
                throw new UnauthorizedException("No membership for this group associated with the user profile");
            }


            Role role = groupMembership.getRole();

            for (Role selectedRole : requiredRoles) {
                if(role.equals(selectedRole)) {
                    return true;
                }
            }

        }

        return false;
    }
}
