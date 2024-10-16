package com.example.demo.service.impl;

import com.example.demo.converters.ConverterService;
import com.example.demo.exceptions.NoGroupFoundException;
import com.example.demo.exceptions.NoUserFoundException;
import com.example.demo.models.dao.User;
import com.example.demo.models.dao.UserGroupRelation;
import com.example.demo.models.dto.UserGroupRelationRequest;
import com.example.demo.models.dto.UserGroupRelationResponse;

import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.UserGroupRelationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.utils.Helper;
import com.example.demo.service.UserGroupRelationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class UserGroupRelationServiceImpl implements UserGroupRelationService {

   private final UserRepository userRepository;
   private final GroupRepository groupRepository;
   private final UserGroupRelationRepository userGroupRelationRepository;
    private final ConverterService converterService;
    private final MongoTemplate mongoTemplate;


}
