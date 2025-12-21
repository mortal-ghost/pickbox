package com.example.pickbox.dao;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.pickbox.models.UserCredential;

@Repository
public interface UserCredentialDao extends MongoRepository<UserCredential, String> {
    @Query("{ 'email': ?0 }")
    UserCredential findByEmail(String email);

    @Query("{ 'userId': ?0 }")
    UserCredential findByUserId(String userId);
}
