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
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/auth")
@RestController
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public UserDto login(@RequestBody LoginRequest loginRequest) {
        log.info("method=login, message=Login attempt, id={}, any other info={}", loginRequest.getEmail(),
                "attempting login");
        if (loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
            throw new IllegalArgumentException("Email and password are required");
        }
        return authService.login(loginRequest.getEmail(), loginRequest.getPassword());
    }

    @PostMapping("/register")
    public UserDto register(@RequestBody RegisterRequest registerRequest) {
        log.info("method=register, message=Register attempt, id={}, any other info={}",
                registerRequest.getEmail(), "attempting registration");
        if (registerRequest.getUsername() == null || registerRequest.getEmail() == null
                || registerRequest.getPassword() == null) {
            throw new IllegalArgumentException("Username, email and password are required");
        }
        return authService.register(registerRequest.getEmail(), registerRequest.getPassword(), registerRequest.getUsername());
    }

    @GetMapping("/me")
    public UserDto me(@RequestHeader("Authorization") String token) {
        log.info("method=me, message=Get current user, id={}, any other info={}", "unknown", "validating token");
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token is required");
        }
        String jwt = token.substring("Bearer ".length());
        return authService.getCurrentUser(jwt);
    }
}
