package com.example.demo.security.utils;

import com.example.demo.models.dto.SimpleIdEmailDto;
import com.example.demo.security.services.impl.UserDetailsImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@AllArgsConstructor
public class Helper {

    public static SimpleIdEmailDto getLoggedSimpleUserIdEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            SimpleIdEmailDto simpleIdNameDto = new SimpleIdEmailDto();
            simpleIdNameDto.setUserId(userDetails.getId());
            simpleIdNameDto.setEmail(userDetails.getEmail());
            return simpleIdNameDto;
        }
        log.error("Failed to get logged-in user details: Authentication is null or principal is not an instance of UserDetailsImpl.");
        return null;
    }

    public static String getLoggedInUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) authentication.getPrincipal()).getId();
        }
        return null;
    }

    public static String getLoggedInUserProfileId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return (((UserDetailsImpl) authentication.getPrincipal()).getUserProfileId());
        }
        return null;
    }

    public static String generateRandomString() {
        return RandomStringUtils.randomAlphanumeric(20);
    }
}
