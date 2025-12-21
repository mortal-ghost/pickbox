package com.example.pickbox.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.pickbox.dtos.LoginRequest;
import com.example.pickbox.dtos.RegisterRequest;
import com.example.pickbox.dtos.UserDto;
import com.example.pickbox.services.AuthService;

@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public UserDto login(@RequestBody LoginRequest loginRequest) {
        if (loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
            throw new IllegalArgumentException("Email and password are required");
        }
        return authService.login(loginRequest.getEmail(), loginRequest.getPassword());
    }

    @PostMapping("/register")
    public UserDto register(@RequestBody RegisterRequest registerRequest) {
        if (registerRequest.getUsername() == null || registerRequest.getEmail() == null || registerRequest.getPassword() == null) {
            throw new IllegalArgumentException("Username, email and password are required");
        }
        return authService.register(registerRequest.getUsername(), registerRequest.getEmail(), registerRequest.getPassword());
    }

    @GetMapping("/me")
    public UserDto me(@RequestHeader("Authorization") String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token is required");
        }
        String jwt = token.substring("Bearer ".length());
        return authService.getCurrentUser(jwt);
    }
}
