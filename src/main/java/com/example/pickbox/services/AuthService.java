package com.example.pickbox.services;

import com.example.pickbox.dtos.UserDto;

public interface AuthService {
    UserDto login(String email, String password);
    UserDto register(String email, String password, String username);
    UserDto getCurrentUser(String token);
}
