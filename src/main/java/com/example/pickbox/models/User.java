package com.example.pickbox.models;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Document(collection = "users")
public class User {
    @Id
    private String id;
    
    private String username;
    private String email;

    private UserStatus status;

    private Instant createdAt;
    private Instant updatedAt;
}
