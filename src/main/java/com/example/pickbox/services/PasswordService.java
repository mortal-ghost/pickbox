package com.example.pickbox.services;

public interface PasswordService {
    String hashPassword(String password, String salt);

    boolean verifyPassword(String inputPassword, String storedHash, String salt);

    String generateSalt();
}
