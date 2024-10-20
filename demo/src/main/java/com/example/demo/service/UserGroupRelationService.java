package com.example.demo.service;

import com.example.demo.models.dto.UserGroupRelationDto;
import com.example.demo.models.dto.UserGroupRelationRequest;
import com.example.demo.models.dto.UserGroupRelationResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserGroupRelationService {
    List<UserGroupRelationDto> getMembershipsByGroupId(String groupId, Pageable pageable);
}
