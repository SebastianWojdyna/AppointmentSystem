package com.example.appointmentsystem.controller;

import com.example.appointmentsystem.dto.UpdateUserRoleRequest;
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

import java.util.List;
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
    public ResponseEntity<List<Map<String, Object>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        logger.info("Fetching user list with specializations, page: {}, size: {}", page, size);

        List<Map<String, Object>> usersWithDetails = userService.getAllUsersWithSpecialization(pageable);
        return ResponseEntity.ok(usersWithDetails);
    }



    @PostMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> addUser(@RequestBody Map<String, Object> userRequest) {
        try {
            logger.info("Adding new user: {}", userRequest.get("email"));

            User user = new User();
            user.setUsername((String) userRequest.get("username"));
            user.setEmail((String) userRequest.get("email"));
            user.setPassword((String) userRequest.get("password"));
            user.setRole(Role.valueOf(((String) userRequest.get("role")).toUpperCase()));

            String specialization = (String) userRequest.get("specialization");

            User savedUser = userService.registerUser(user, specialization);

            return ResponseEntity.ok(Map.of("message", "Użytkownik został dodany", "user", savedUser));
        } catch (Exception e) {
            logger.error("Error adding user", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> userRequest) {
        try {
            logger.info("Updating user with ID: {}", id);

            User userToUpdate = new User();
            userToUpdate.setUsername((String) userRequest.get("username"));
            userToUpdate.setEmail((String) userRequest.get("email"));
            userToUpdate.setRole(Role.valueOf(((String) userRequest.get("role")).toUpperCase()));

            String specialization = (String) userRequest.get("specialization");

            User updatedUser = userService.updateUserWithSpecialization(id, userToUpdate, specialization);

            return ResponseEntity.ok(Map.of("message", "Użytkownik został edytowany", "user", updatedUser));
        } catch (Exception e) {
            logger.error("Error updating user", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            logger.info("Deleting user with ID: {}", id);
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "Użytkownik został usunięty"));
        } catch (Exception e) {
            logger.error("Error deleting user", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
