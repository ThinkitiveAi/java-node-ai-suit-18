package com.healthfirst.provider.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;

@Component
public class JwtUtil {
    private static final String SECRET = "super-secret-key-for-provider-jwt-auth-should-be-long";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
    private static final long DEFAULT_EXPIRATION_MS = 3600_000; // 1 hour

    public static String generateToken(Map<String, Object> claims, String subject) {
        return generateToken(claims, subject, DEFAULT_EXPIRATION_MS);
    }
    
    public static String generateToken(Map<String, Object> claims, long expirationSeconds) {
        return generateToken(claims, "health-first", expirationSeconds * 1000);
    }
    
    public static String generateToken(Map<String, Object> claims, String subject, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims validateToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static long getDefaultExpirationMs() {
        return DEFAULT_EXPIRATION_MS;
    }
} 