package com.example.appointmentsystem.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long expirationTime; // W milisekundach

    /**
     * Generowanie tokena JWT.
     *
     * @param email email użytkownika
     * @param role  rola użytkownika
     * @return token JWT
     */
    public String generateToken(String email, String role) {
        try {
            logger.info("Generating JWT token for email: {}, role: {}", email, role);
            String token = Jwts.builder()
                    .setSubject(email) // Ustawienie email jako subject
                    .claim("role", role)
                    .setIssuedAt(new Date()) // Ustawienie czasu wydania
                    .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // Ustawienie czasu wygaśnięcia
                    .signWith(SignatureAlgorithm.HS512, jwtSecret) // Podpisanie tokena algorytmem HS512
                    .compact();

            logger.info("Generated token: {}", token);
            return token;
        } catch (Exception e) {
            logger.error("Error generating JWT token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    /**
     * Wyodrębnianie zawartości tokena (claims).
     *
     * @param token token JWT
     * @return obiekt Claims
     */
    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(jwtSecret) // Ustawienie klucza do weryfikacji
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    /**
     * Wyodrębnianie emaila użytkownika z tokena.
     *
     * @param token token JWT
     * @return email użytkownika
     */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject(); // Pobranie emaila (subject) z tokena
    }

    /**
     * Wyodrębnianie roli użytkownika z tokena.
     *
     * @param token token JWT
     * @return rola użytkownika
     */
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class); // Pobranie roli z claim
    }

    /**
     * Walidacja tokena JWT.
     *
     * @param token token JWT
     * @param email email użytkownika
     * @return true, jeśli token jest ważny
     */
    public boolean isTokenValid(String token, String email) {
        try {
            logger.info("Validating token for email: {}", email);
            String extractedEmail = extractEmail(token);
            boolean isValid = extractedEmail.equals(email) && !isTokenExpired(token);
            logger.info("Token is valid: {}", isValid);
            return isValid;
        } catch (Exception e) {
            logger.error("Error validating token: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Sprawdzenie, czy token wygasł.
     *
     * @param token token JWT
     * @return true, jeśli token wygasł
     */
    private boolean isTokenExpired(String token) {
        Date expiration = extractClaims(token).getExpiration();
        return expiration.before(new Date());
    }
}
