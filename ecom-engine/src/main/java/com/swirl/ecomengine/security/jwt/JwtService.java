package com.swirl.ecomengine.security.jwt;

import com.swirl.ecomengine.auth.exception.JwtValidationException;
import com.swirl.ecomengine.user.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private final String secret;
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs
    ) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    // ------------------------------------------------------------
    // Key
    // ------------------------------------------------------------
    private Key signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // ------------------------------------------------------------
    // Generate token
    // ------------------------------------------------------------
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(expirationDate())
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Date expirationDate() {
        return new Date(System.currentTimeMillis() + expirationMs);
    }

    // ------------------------------------------------------------
    // Extract userId
    // ------------------------------------------------------------
    public Long extractUserId(String token) {
        try {
            return Long.valueOf(extractClaims(token).getSubject());
        } catch (Exception e) {
            throw new JwtValidationException("Invalid token subject", e);
        }
    }

    // ------------------------------------------------------------
    // Extract email
    // ------------------------------------------------------------
    public String extractEmail(String token) {
        try {
            return extractClaims(token).get("email", String.class);
        } catch (Exception e) {
            throw new JwtValidationException("Invalid email claim", e);
        }
    }

    // ------------------------------------------------------------
    // Extract role
    // ------------------------------------------------------------
    public String extractRole(String token) {
        try {
            return extractClaims(token).get("role", String.class);
        } catch (Exception e) {
            throw new JwtValidationException("Invalid role claim", e);
        }
    }

    // ------------------------------------------------------------
    // Validate token
    // ------------------------------------------------------------
    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtValidationException("JWT validation failed", e);
        }
    }

    // ------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}