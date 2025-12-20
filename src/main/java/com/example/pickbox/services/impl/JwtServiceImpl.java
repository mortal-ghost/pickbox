package com.example.pickbox.services.impl;

import java.security.Key;
import java.sql.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.pickbox.models.User;
import com.example.pickbox.services.JwtService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtServiceImpl implements JwtService {
    private final Key secretKey;

    private final long expiration;

    public JwtServiceImpl(@Value("${jwt.secret}")String secretKey, 
                          @Value("${jwt.expiration}") long expiration) {
        byte[] secretKeyBytes = Decoders.BASE64.decode(secretKey);
        this.secretKey = Keys.hmacShaKeyFor(secretKeyBytes);
        this.expiration = expiration;
    }

    private String generateToken(Map<String, String> claims, String userId) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String generateToken(User user) {
        Map<String, String> claims = Map.of("userName", user.getUsername());
        return generateToken(claims, user.getId());
    }
    
    @Override
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
