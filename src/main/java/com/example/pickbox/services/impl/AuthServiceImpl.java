package com.example.pickbox.services.impl;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.example.pickbox.constants.AuthErrorMessages;
import com.example.pickbox.dao.UserCredentialDao;
import com.example.pickbox.dao.UserDao;
import com.example.pickbox.dtos.UserDto;
import com.example.pickbox.exceptions.AuthException;
import com.example.pickbox.models.User;
import com.example.pickbox.models.UserCredential;
import com.example.pickbox.models.UserStatus;
import com.example.pickbox.services.AuthService;
import com.example.pickbox.services.JwtService;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserCredentialDao userCredentialDao;

    private final UserDao userDao;

    private final JwtService jwtService;

    public AuthServiceImpl(UserCredentialDao userCredentialDao, UserDao userDao, JwtService jwtService) {
        this.userCredentialDao = userCredentialDao;
        this.userDao = userDao;
        this.jwtService = jwtService;
    }

    @Override
    public UserDto login(String email, String password) {
        UserCredential userCredential = userCredentialDao.findByEmail(email);
        if (userCredential == null || userCredential.getUserId() == null) {
            throw new AuthException(AuthErrorMessages.USER_NOT_FOUND);
        }

        if (!verifyPassword(password, userCredential.getPassword(), userCredential.getSalt())) {
            throw new AuthException(AuthErrorMessages.INVALID_CREDENTIALS);
        }
        User user = userDao.findById(userCredential.getUserId()).orElse(null);
        if (user == null) {
            throw new AuthException(AuthErrorMessages.USER_NOT_FOUND);
        }
        String token = jwtService.generateToken(user);
        UserDto userDto = new UserDto(user);
        userDto.setToken(token);
        return userDto;
    }

    private boolean verifyPassword(String inputPassword, String storedHash, String salt) {
        String inputHash = hashPassword(inputPassword, salt);
        return inputHash.equals(storedHash);
    }

    private String hashPassword(String password, String salt) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            md.update(java.util.Base64.getDecoder().decode(salt));
            byte[] hashedPassword = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hashedPassword);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private String generateSalt() {
        return java.util.UUID.randomUUID().toString();
    }

    @Override
    public UserDto register(String email, String password, String username) {
        UserCredential userCredential = userCredentialDao.findByEmail(email);
        if (userCredential != null) {
            throw new AuthException(AuthErrorMessages.USER_ALREADY_EXISTS);
        }
        String salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);
        User user = User.builder()
                .username(username)
                .email(email)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .status(UserStatus.ACTIVE)
                .build();
        user = userDao.save(user);
        userCredential = UserCredential.builder()
                .email(email)
                .password(hashedPassword)
                .salt(salt)
                .userId(user.getId())
                .build();
        userCredential = userCredentialDao.save(userCredential);
        String token = jwtService.generateToken(user);
        UserDto userDto = new UserDto(user);
        userDto.setToken(token);
        return userDto;
    }
}
