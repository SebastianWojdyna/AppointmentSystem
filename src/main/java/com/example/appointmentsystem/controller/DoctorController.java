package com.example.appointmentsystem.controller;

import com.example.appointmentsystem.model.Doctor;
import com.example.appointmentsystem.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/doctor/me")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<Doctor> getCurrentDoctor(Authentication authentication) {
        String email = authentication.getName();

        // Znalezienie doktora na podstawie emaila użytkownika
        Doctor doctor = doctorService.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        return ResponseEntity.ok(doctor);
    }
}
