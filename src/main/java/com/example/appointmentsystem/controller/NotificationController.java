package com.example.appointmentsystem.controller;

import com.example.appointmentsystem.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<String> handleNotification(@RequestBody Map<String, Object> notificationData) {
        if (notificationData == null) {
            return ResponseEntity.badRequest().body("Notification data is missing");
        }

        System.out.println("Received notification payload: " + notificationData);

        Object orderIdObj = notificationData.get("orderId");
        Object paidObj = notificationData.get("paid");

        if (orderIdObj == null || paidObj == null) {
            return ResponseEntity.badRequest().body("Required fields are missing in notification data");
        }

        String orderId = orderIdObj.toString();
        boolean paid = Boolean.parseBoolean(paidObj.toString());

        appointmentService.updatePaymentStatus(orderId, paid);

        return ResponseEntity.ok("Notification received and processed");
    }
}
