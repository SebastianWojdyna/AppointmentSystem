package com.example.appointmentsystem.controller;

import com.example.appointmentsystem.dto.AvailabilityRequest;
import com.example.appointmentsystem.model.Availability;
import com.example.appointmentsystem.model.Doctor;
import com.example.appointmentsystem.model.AppointmentServiceType;
import com.example.appointmentsystem.model.User;
import com.example.appointmentsystem.repository.AvailabilityRepository;
import com.example.appointmentsystem.service.AvailabilityService;
import com.example.appointmentsystem.service.DoctorService;
import com.example.appointmentsystem.service.ServiceService;
import com.example.appointmentsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    @Autowired
    private AvailabilityService availabilityService;

    private final AvailabilityRepository availabilityRepository;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private UserService userService;

    @Autowired
    public AvailabilityController(AvailabilityRepository availabilityRepository) {
        this.availabilityRepository = availabilityRepository;
    }

    // Dodanie dostępności przez lekarza
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<?> addDoctorAvailability(@RequestBody AvailabilityRequest request, Authentication authentication) {
        // Pobranie username zalogowanego użytkownika z tokena JWT
        String username = authentication.getName();

        // Znalezienie użytkownika na podstawie username
        User doctorUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Znalezienie profilu lekarza powiązanego z user_id
        Doctor doctor = doctorService.findByUserId(doctorUser.getId())
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        // Znalezienie usługi na podstawie ID
        AppointmentServiceType service = serviceService.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        // Dodanie dostępności dla lekarza
        availabilityService.addDoctorAvailability(doctor, service, request.getAvailableTimes(), request.getPrice());

        return ResponseEntity.ok("Availability added successfully");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteAvailability(@PathVariable Long id) {
        if (!availabilityRepository.existsById(id)) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Dostępność nie istnieje"));
        }

        availabilityRepository.deleteById(id);
        return ResponseEntity.ok(Collections.singletonMap("message", "Dostępność została usunięta"));
    }


    // Pobranie dostępności dla lekarza
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Availability>> getDoctorAvailability(@PathVariable Long doctorId) {
        return ResponseEntity.ok(availabilityService.getDoctorAvailability(doctorId));
    }

    // Pobranie dostępności dla usługi
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<Availability>> getServiceAvailability(@PathVariable Long serviceId) {
        return ResponseEntity.ok(availabilityService.getServiceAvailability(serviceId));
    }

}
