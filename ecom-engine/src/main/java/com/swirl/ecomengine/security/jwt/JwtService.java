package com.swirl.ecomengine.security.jwt;

import com.swirl.ecomengine.auth.exception.JwtValidationException;
import com.swirl.ecomengine.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
@Profile({"dev", "prod", "test-integration"})
public class JwtService {

    private final String secret;
    private final long expirationMs;

    // ------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------

    @Autowired
    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs
    ) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    // ------------------------------------------------------------
    // Key handling
    // ------------------------------------------------------------

    private Key signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // ------------------------------------------------------------
    // Token generation
    // ------------------------------------------------------------

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
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
    // Token validation + claim extraction
    // ------------------------------------------------------------

    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtValidationException("JWT validation failed", e);
        }
    }

    public String extractEmail(String token) {
        try {
            return extractClaims(token).getSubject();
        } catch (Exception e) {
            throw new JwtValidationException("Invalid token subject", e);
        }
    }

    public String extractRole(String token) {
        try {
            return extractClaims(token).get("role", String.class);
        } catch (Exception e) {
            throw new JwtValidationException("Invalid or missing role claim", e);
        }
    }

    // ------------------------------------------------------------
    // Internal claim parsing
    // ------------------------------------------------------------

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}