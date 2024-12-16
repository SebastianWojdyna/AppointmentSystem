package com.example.appointmentsystem.controller;

import com.example.appointmentsystem.model.Doctor;
import com.example.appointmentsystem.service.DoctorService;
import org.hibernate.Hibernate;
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

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<Doctor> getCurrentDoctor(Authentication authentication) {
        String username = authentication.getName(); // Pobranie nazwy użytkownika z tokena JWT

        // Znalezienie doktora na podstawie nazwy użytkownika
        Doctor doctor = doctorService.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Jawne wczytanie relacji lazy-loaded
        Hibernate.initialize(doctor.getUser());
        Hibernate.initialize(doctor.getServices());

        return ResponseEntity.ok(doctor);
    }
}
