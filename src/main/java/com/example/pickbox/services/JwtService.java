package com.example.pickbox.services;

import com.example.pickbox.models.User;

public interface JwtService {
    String generateToken(User user);

    String extractUsername(String token);

    boolean validateToken(String token);
}
