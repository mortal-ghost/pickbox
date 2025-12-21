package com.example.pickbox.services.impl;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.example.pickbox.services.PasswordService;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserCredentialDao userCredentialDao;

    private final UserDao userDao;

    private final JwtService jwtService;
    private final PasswordService passwordService;
            

    public AuthServiceImpl(UserCredentialDao userCredentialDao, UserDao userDao, JwtService jwtService, PasswordService passwordService) {
        this.userCredentialDao = userCredentialDao;
        this.userDao = userDao;
        this.jwtService = jwtService;
        this.passwordService = passwordService;
    }

    @Override
    public UserDto login(String email, String password) {
        UserCredential userCredential = userCredentialDao.findByEmail(email);
        if (userCredential == null || userCredential.getUserId() == null) {
            throw new AuthException(AuthErrorMessages.USER_NOT_FOUND);
        }

        if (!passwordService.verifyPassword(password, userCredential.getPassword(), userCredential.getSalt())) {
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

    @Override
    @Transactional
    public UserDto register(String email, String password, String username) {
        UserCredential userCredential = userCredentialDao.findByEmail(email);
        if (userCredential != null) {
            throw new AuthException(AuthErrorMessages.USER_ALREADY_EXISTS);
        }
        String salt = passwordService.generateSalt();
        String hashedPassword = passwordService.hashPassword(password, salt);
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

    @Override
    public UserDto getCurrentUser(String token) {
        String userId = jwtService.extractUsername(token);
        if (userId == null) {
            throw new AuthException(AuthErrorMessages.TOKEN_INVALID);
        }
        User user = userDao.findById(userId).orElse(null);
        if (user == null) {
            throw new AuthException(AuthErrorMessages.USER_NOT_FOUND);
        }
        return new UserDto(user);
    }
}
