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

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserCredentialDao userCredentialDao;

    private final UserDao userDao;

    private final JwtService jwtService;
    private final PasswordService passwordService;

    public AuthServiceImpl(UserCredentialDao userCredentialDao, UserDao userDao, JwtService jwtService,
            PasswordService passwordService) {
        this.userCredentialDao = userCredentialDao;
        this.userDao = userDao;
        this.jwtService = jwtService;
        this.passwordService = passwordService;
    }

    @Override
    public UserDto login(String email, String password) {
        log.info("method=login, message=Login service called, id={}", email);
        try {
            UserCredential userCredential = userCredentialDao.findByEmail(email);
            if (userCredential == null) {
                log.error("method=login, message=UserCredential not found for email, id={}", email);
                throw new AuthException(AuthErrorMessages.USER_NOT_FOUND);
            }
            if (userCredential.getUserId() == null) {
                log.error("method=login, message=UserCredential has null userId, id={}", email);
                throw new AuthException(AuthErrorMessages.USER_NOT_FOUND);
            }

            if (!passwordService.verifyPassword(password, userCredential.getPassword(), userCredential.getSalt())) {
                log.error("method=login, message=Invalid credentials, id={}", email);
                throw new AuthException(AuthErrorMessages.INVALID_CREDENTIALS);
            }
            User user = userDao.findById(userCredential.getUserId()).orElse(null);
            if (user == null) {
                log.error("method=login, message=User object not found for userId, id={}, userId={}", email,
                        userCredential.getUserId());
                throw new AuthException(AuthErrorMessages.USER_NOT_FOUND);
            }
            log.info("method=login, message=User found and verified, id={}", email);
            String token = jwtService.generateToken(user);
            UserDto userDto = new UserDto(user);
            userDto.setToken(token);
            return userDto;
        } catch (Exception e) {
            log.error("method=login, message=Error during login, id={}, error={}", email, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public UserDto register(String email, String password, String username) {
        log.info("method=register, message=Register service called, id={}, any other info={}", email,
                "username=" + username);
        try {
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
            UserCredential newUserCredential = UserCredential.builder()
                    .email(email)
                    .password(hashedPassword)
                    .salt(salt)
                    .userId(user.getId())
                    .build();
            userCredentialDao.save(newUserCredential);
            String token = jwtService.generateToken(user);
            UserDto userDto = new UserDto(user);
            userDto.setToken(token);
            return userDto;
        } catch (Exception e) {
            log.error("method=register, message=Error during registration, id={}, error={}", email, e.getMessage());
            throw e;
        }
    }

    @Override
    public UserDto getCurrentUser(String token) {
        try {
            String userId = jwtService.extractUsername(token);
            log.info("method=getCurrentUser, message=Get User by Token service called, id={}, any other info={}",
                    userId, "processing");
            if (userId == null) {
                throw new AuthException(AuthErrorMessages.TOKEN_INVALID);
            }
            User user = userDao.findById(userId).orElse(null);
            if (user == null) {
                throw new AuthException(AuthErrorMessages.USER_NOT_FOUND);
            }
            return new UserDto(user);
        } catch (Exception e) {
            log.error("method=getCurrentUser, message=Error getting current user, id=unknown, error={}",
                    e.getMessage());
            throw e;
        }
    }
}
