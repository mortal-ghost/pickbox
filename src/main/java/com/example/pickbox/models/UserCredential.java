package com.example.pickbox.models;

import java.time.Instant;

import org.springframework.data.annotation.Id;

import com.mongodb.lang.NonNull;

import lombok.Data;

@Data
public class UserCredential {
    @Id
    private String id;
    @NonNull
    private String userId;

    private String password;

    private String salt;

    private Instant createdAt;
    private Instant updatedAt;
}
