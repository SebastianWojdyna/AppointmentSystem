package com.example.appointmentsystem.controller;

import com.example.appointmentsystem.model.Appointment;
import com.example.appointmentsystem.model.Doctor;
import com.example.appointmentsystem.model.User;
import com.example.appointmentsystem.service.AppointmentService;
import com.example.appointmentsystem.service.DoctorService;
import com.example.appointmentsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private UserService userService;

    // Dodawanie dostępności przez lekarza
    @PostMapping("/doctor/availability")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<?> setDoctorAvailability(@RequestBody List<LocalDateTime> availableTimes, Authentication authentication) {
        User doctorUser = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Doctor user not found"));
        Doctor doctor = doctorService.findByUserEmail(doctorUser.getEmail())
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        appointmentService.addDoctorAvailability(doctor, availableTimes);
        return ResponseEntity.ok("Availability added successfully");
    }

    // Pobieranie dostępnych terminów wizyt dla danego lekarza
    @GetMapping("/doctor/{doctorId}/available")
    @PreAuthorize("hasAuthority('ROLE_PATIENT') or hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<List<Appointment>> getAvailableAppointments(@PathVariable Long doctorId) {
        List<Appointment> appointments = appointmentService.getAvailableAppointmentsByDoctorId(doctorId);
        return ResponseEntity.ok(appointments);
    }

    // Rezerwacja terminu przez pacjenta
    @PostMapping("/{appointmentId}/book")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public ResponseEntity<?> bookAppointment(@PathVariable Long appointmentId, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        appointmentService.bookAppointment(appointmentId, user);
        return ResponseEntity.ok("Appointment successfully booked");
    }
}
