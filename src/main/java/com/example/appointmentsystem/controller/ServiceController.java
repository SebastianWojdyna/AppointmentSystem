package com.example.appointmentsystem.controller;

import com.example.appointmentsystem.model.AppointmentServiceType;
import com.example.appointmentsystem.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceService serviceService;

    @Autowired
    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public ResponseEntity<List<AppointmentServiceType>> getAllServices() {
        List<AppointmentServiceType> services = serviceService.findAll();
        return ResponseEntity.ok(services);
    }
}
