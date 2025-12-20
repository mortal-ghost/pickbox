package com.example.pickbox.dtos;

import java.time.Instant;

import lombok.Data;
import com.example.pickbox.models.User;

@Data
public class UserDto {
    private String id;
    private String username;
    private String email;
    private Instant createdAt;
    private String token;

    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.createdAt = user.getCreatedAt();
    }
}
