package com.example.pickbox.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.pickbox.models.User;

public interface UserDao extends MongoRepository<User, String> {
}
