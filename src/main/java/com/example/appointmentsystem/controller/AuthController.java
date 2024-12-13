package com.example.appointmentsystem.controller;

import com.example.appointmentsystem.repository.UserRepository;
import com.example.appointmentsystem.service.UserService;
import com.example.appointmentsystem.model.Role;
import com.example.appointmentsystem.model.User;
import com.example.appointmentsystem.security.JwtUtil;
import com.example.appointmentsystem.service.UserService;
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = (User) authentication.getPrincipal();
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("role", user.getRole());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Haszowanie hasła
        userRepository.save(user); // Zapis do bazy danych
        return ResponseEntity.ok(user);
    }
}