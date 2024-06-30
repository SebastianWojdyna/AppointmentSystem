package com.example.appointmentsystem.controller;

import com.example.appointmentsystem.dto.PaymentStatusUpdateRequest;
import com.example.appointmentsystem.model.Appointment;
import com.example.appointmentsystem.service.AppointmentService;
import com.example.appointmentsystem.service.PayUService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PayUService payUService;

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping(value = "/create-appointment", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> createAppointment(@RequestBody Appointment appointment) {
        logger.info("createAppointment method called with appointment: {}", appointment);
        // Ustawienie statusu płatności na true
        appointment.setPaid(true);
        appointmentService.saveAppointment(appointment); // Zapisanie wizyty z ustawionym statusem
        String redirectUrl = payUService.initiatePayment(appointment);
        Map<String, String> response = new HashMap<>();
        response.put("redirectUrl", redirectUrl);
        response.put("orderId", appointment.getOrderId());
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/process", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> processPayment(@RequestBody Map<String, String> request) {
        String orderId = request.get("orderId");
        if (orderId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Order ID is missing"));
        }
        String redirectUrl = payUService.processPayment(orderId);
        Map<String, String> response = new HashMap<>();
        response.put("redirectUrl", redirectUrl);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/update-status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updatePaymentStatus(@RequestBody PaymentStatusUpdateRequest request) {
        logger.info("updatePaymentStatus method called with request: {}", request);
        try {
            appointmentService.updatePaymentStatus(request.getOrderId(), request.isPaid());
            logger.info("Payment status updated for orderId: {}, isPaid: {}", request.getOrderId(), request.isPaid());
        } catch (Exception e) {
            logger.error("Error updating payment status for orderId: {}", request.getOrderId(), e);
        }
        return ResponseEntity.ok().build();
    }
}
