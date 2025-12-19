package com.example.pickbox.dao;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.pickbox.models.UserCredential;

@Repository
public interface UserDao extends MongoRepository<UserCredential, String> {
    UserCredential findByEmail(String email);
}
