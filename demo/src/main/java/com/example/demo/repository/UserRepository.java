package com.example.demo.repository;

import com.example.demo.models.dao.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    User findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsById(String id);
    User getUserById(String id);
    List<User> findByFcmTokenIsNotNull();
}
