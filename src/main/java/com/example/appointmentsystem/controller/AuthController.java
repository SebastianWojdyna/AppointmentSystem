package com.example.appointmentsystem.controller;

import com.example.appointmentsystem.model.Role;
import com.example.appointmentsystem.model.User;
import com.example.appointmentsystem.repository.UserRepository;
import com.example.appointmentsystem.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Logowanie użytkownika.
     * Sprawdza dane uwierzytelniające (email i hasło) i generuje token JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email"); // Zmiana na email
        String password = loginRequest.get("password");

        try {
            // Autoryzacja użytkownika za pomocą email i hasła
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Pobierz zalogowanego użytkownika z bazy
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            // Generowanie tokena JWT
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

            // Przygotowanie odpowiedzi
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("role", user.getRole());
            response.put("email", user.getEmail());
            response.put("username", user.getUsername());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // W przypadku błędu zwróć status 401
            return ResponseEntity.status(401).body("Invalid email or password");
        }
    }

    /**
     * Rejestracja nowego użytkownika.
     * Hashuje hasło i zapisuje użytkownika w bazie danych.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> registrationRequest) {
        String username = registrationRequest.get("username");
        String email = registrationRequest.get("email");
        String password = registrationRequest.get("password");
        String role = registrationRequest.get("role");

        // Sprawdź, czy użytkownik o podanym emailu lub nazwie użytkownika już istnieje
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body("Username is already taken");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body("Email is already in use");
        }

        // Utwórz nowego użytkownika
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password)); // Hashowanie hasła

        try {
            user.setRole(Role.valueOf(role.toUpperCase())); // Ustawienie roli
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid role provided");
        }

        // Zapisz użytkownika w bazie danych
        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }
}
