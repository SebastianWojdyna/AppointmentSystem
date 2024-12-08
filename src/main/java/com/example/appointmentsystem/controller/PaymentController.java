package com.example.appointmentsystem.controller;

import com.example.appointmentsystem.dto.PaymentStatusUpdateRequest;
import com.example.appointmentsystem.model.Appointment;
import com.example.appointmentsystem.model.Doctor;
import com.example.appointmentsystem.model.User;
import com.example.appointmentsystem.service.AppointmentService;
import com.example.appointmentsystem.service.DoctorService;
import com.example.appointmentsystem.service.PayUService;
import com.example.appointmentsystem.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    @PostMapping(value = "/create-appointment", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> createAppointment(@RequestBody Map<String, Object> appointmentData) {
        logger.info("createAppointment method called with appointmentData: {}", appointmentData);

        try {
            Integer userIdInt = Integer.parseInt(appointmentData.get("userId").toString());
            Integer doctorIdInt = Integer.parseInt(appointmentData.get("doctorId").toString());
            Integer serviceIdInt = Integer.parseInt(appointmentData.get("serviceId").toString());

            if (userIdInt == null || doctorIdInt == null || serviceIdInt == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User ID, Doctor ID or Service ID is missing"));
            }

            Long userId = userIdInt.longValue();
            Long doctorId = doctorIdInt.longValue();
            Long serviceId = serviceIdInt.longValue();

            User user = userService.findUserById(userId);
            Doctor doctor = doctorService.findDoctorById(doctorId);

            if (user == null || doctor == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User or Doctor not found"));
            }

            // Tworzenie nowej wizyty
            Appointment appointment = new Appointment();
            appointment.setPatientName((String) appointmentData.get("patientName"));
            appointment.setAppointmentTime(LocalDateTime.parse((String) appointmentData.get("appointmentTime")));
            appointment.setPaid((Boolean) appointmentData.get("paid"));
            appointment.setOrderId((String) appointmentData.get("orderId"));
            appointment.setUser(user);
            appointment.setDoctor(doctor);
            appointment.setService(appointmentService.findServiceById(serviceId));

            // Zapisanie wizyty
            Appointment savedAppointment = appointmentService.saveAppointment(appointment);
            logger.info("Appointment saved with ID: {}", savedAppointment.getId());

            // Inicjacja platnosci
            String redirectUrl = payUService.initiatePayment(savedAppointment);
            Map<String, String> response = new HashMap<>();
            response.put("redirectUrl", redirectUrl);
            response.put("orderId", savedAppointment.getOrderId());

            logger.info("Response to be sent: {}", response);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creating appointment: ", e);
            return ResponseEntity.status(500).body(Map.of("error", "Internal Server Error"));
        }
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
