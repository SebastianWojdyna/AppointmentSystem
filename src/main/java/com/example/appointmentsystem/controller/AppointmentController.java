package com.example.appointmentsystem.controller;

import com.example.appointmentsystem.model.Appointment;
import com.example.appointmentsystem.model.User;
import com.example.appointmentsystem.service.AppointmentService;
import com.example.appointmentsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<Appointment>> getUserAppointments(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(appointmentService.getAppointmentsByUserId(user.getId()));  // Pobieranie wizyt użytkownika
    }

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody Appointment appointment, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        appointment.setUser(user);  // Ustawienie użytkownika
        return ResponseEntity.ok(appointmentService.saveAppointment(appointment));
    }

    @PutMapping("/payment/{orderId}")
    public ResponseEntity<Void> updatePaymentStatus(@PathVariable String orderId, @RequestParam boolean isPaid) {
        appointmentService.updatePaymentStatus(orderId, isPaid);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        appointmentService.deleteAppointment(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
