package com.example.demo.service.impl;

import com.amazonaws.services.kms.model.NotFoundException;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.models.dao.User;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.UserGroupRelationRepository;
import com.example.demo.security.utils.Helper;
import com.example.demo.service.AmazonS3Service;
import com.example.demo.service.UserService;
import com.example.demo.converters.ConverterService;
import com.example.demo.models.dto.UserDto;
import com.example.demo.models.dto.UserProfileEditResponse;
import com.example.demo.models.dto.UserProfileRequest;
import com.example.demo.models.dto.UserProfileResponse;
import com.example.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;
import java.util.Optional;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final ConverterService converterService;

    private final AmazonS3Service amazonS3Service;

    private final MongoTemplate mongoTemplate;

    private final UserGroupRelationRepository groupMembershipRepository;

    private final GroupRepository groupRepository;


    @Override
    public UserDto getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        log.info("Get user by id finished");
        return converterService.convertToUserDto(user);
    }

    @Override
    public boolean checkEmail(String email) {
        boolean exists = userRepository.existsByEmail(email);

        if (!exists) {
            throw new NotFoundException("The provided email is not correct");
        }
        return true;
    }


    @Override
    public String getUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if(user == null) {
            throw new NotFoundException("User not found with email: " + email);
        }
        return user.getId();
    }

    @Override
    public byte[] downloadUserProfilePhoto() throws IOException {
        User user = userRepository.getUserById(Helper.getLoggedInUserId());


        String fileUri = user.getPhotoUri();
        if (fileUri == null || fileUri.trim().isEmpty()) {
            throw new IllegalArgumentException("The photoUri field is missing or empty in the UserProfile.");
        }
        log.info("fileUri: {}", fileUri);
        return amazonS3Service.downloadFromS3(fileUri);
    }

    @Override
    public void updateFcmToken(String token) {
        User user = userRepository.getUserById(Helper.getLoggedInUserId());
        if(user == null) {
            throw new NotFoundException("Logged user not found");
        }
        user.setFcmToken(token);
        userRepository.save(user);
        log.info("FcmToken successfully updated for the user profile");
    }

    @Override
    public UserProfileEditResponse editUserProfile(String firstName, String lastName, String description, MultipartFile photoFile) {
        User userProfile = userRepository.getUserById(Helper.getLoggedInUserId());

        if (userProfile == null) {
            throw new IllegalStateException("UserProfile is null");
        }

        if (photoFile != null) {
            try {
                String fileName = userProfile.getId();
                String path = "profilePhotos";

                amazonS3Service.updateFileInS3(path, fileName, photoFile.getInputStream());

                userProfile.setPhotoUri(path +  "/"  + fileName);

                log.info("UserProfile photo updated successfully");
            } catch (IOException e) {
                log.error("Error updating the profile photo", e);
                throw new RuntimeException("Failed to update the UserProfile photo", e);
            }
        }

        if (firstName != null && !firstName.trim().isEmpty()) {
            userProfile.setFirstName(firstName);
        }

        if (lastName != null && !lastName.trim().isEmpty()) {
            userProfile.setLastName(lastName);
        }

        userRepository.save(userProfile);

        log.info("UserProfile successfully updated");
        UserProfileEditResponse response = new UserProfileEditResponse();
        String url = converterService.convertPhotoUriToUrl(userProfile.getPhotoUri());
        response.setPhotoUri(url);
        return response;
    }



}
