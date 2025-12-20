package com.example.pickbox.models;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Document(collection = "user_credentials")
public class UserCredential {
    @Id
    private String id;
    @NonNull
    private String userId;

    @NonNull
    private String email;

    private String password;

    private String salt;

    private Instant createdAt;
    private Instant updatedAt;
}
