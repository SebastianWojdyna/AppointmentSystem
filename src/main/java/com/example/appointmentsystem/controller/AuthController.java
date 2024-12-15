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
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

            // Poprawna odpowiedź jako JSON
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("email", user.getEmail());
            response.put("username", user.getUsername());
            response.put("role", user.getRole().name());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Odpowiedź dla błędu logowania
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid email or password");
            return ResponseEntity.status(401).body(errorResponse);
        }
    }

    /**
     * Rejestracja nowego użytkownika z domyślną rolą PATIENT.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> registrationRequest) {
        String username = registrationRequest.get("username");
        String email = registrationRequest.get("email");
        String password = registrationRequest.get("password");

        // Sprawdzenie, czy użytkownik istnieje
        if (userRepository.findByUsername(username).isPresent()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Username is already taken");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        if (userRepository.findByEmail(email).isPresent()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Email is already in use");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Tworzenie nowego użytkownika z rolą PATIENT
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.PATIENT); // Domyślna rola to PATIENT

        userRepository.save(user);

        // Zwrot odpowiedzi jako poprawny JSON
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "User registered successfully with role PATIENT");
        return ResponseEntity.ok(successResponse);
    }
}
