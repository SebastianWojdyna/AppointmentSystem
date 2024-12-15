package com.example.appointmentsystem.controller;

import com.example.appointmentsystem.model.Role;
import com.example.appointmentsystem.model.User;
import com.example.appointmentsystem.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String getAdminDashboard() {
        return "Welcome to the admin dashboard!";
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<User>> getUsers(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        logger.info("Fetching user list, page: {}, size: {}", page, size);
        Page<User> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> addUser(@RequestBody User user) {
        logger.info("Adding new user: {}", user.getEmail());
        User savedUser = userService.registerUser(user);
        return ResponseEntity.ok(Map.of("message", "User added successfully", "user", savedUser));
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        logger.info("Updating user with ID: {}", id);
        User user = userService.updateUser(id, updatedUser);
        return ResponseEntity.ok(Map.of("message", "User updated successfully", "user", user));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        logger.info("Deleting user with ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    /**
     * Aktualizacja roli użytkownika.
     */
    @PatchMapping("/users/{id}/role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> roleRequest) {
        try {
            logger.info("Updating role for user with ID: {}", id);

            String roleString = roleRequest.get("role");
            if (roleString == null || roleString.isEmpty()) {
                logger.warn("No role provided in request");
                return ResponseEntity.badRequest().body(Map.of("error", "Role cannot be empty"));
            }

            Role newRole = Role.valueOf(roleString.toUpperCase());
            User updatedUser = userService.updateUserRole(id, newRole);

            logger.info("Role updated successfully for user: {}, new role: {}", updatedUser.getUsername(), newRole);
            return ResponseEntity.ok(Map.of(
                    "message", "User role updated successfully",
                    "user", updatedUser
            ));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid role provided: {}", roleRequest.get("role"), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role provided"));
        } catch (Exception e) {
            logger.error("Error updating role for user ID: {}", id, e);
            return ResponseEntity.status(500).body(Map.of("error", "Error updating role", "details", e.getMessage()));
        }
    }
}
