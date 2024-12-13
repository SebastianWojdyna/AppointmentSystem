package com.example.appointmentsystem.security;

import com.example.appointmentsystem.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKeyString; // Pobieramy klucz z application.properties

    private SecretKey secretKey;

    private final long expirationTime = 1000 * 60 * 60 * 10; // 10 godzin

    @PostConstruct
    public void init() {
        // Konwertujemy Base64 na SecretKey
        byte[] decodedKey = Base64.getDecoder().decode(secretKeyString);
        secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA256");
    }

    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token, String username) {
        return extractUsername(token).equals(username) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}
