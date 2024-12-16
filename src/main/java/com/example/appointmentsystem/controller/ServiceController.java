package com.example.appointmentsystem.controller;

import com.example.appointmentsystem.model.AppointmentServiceType;
import com.example.appointmentsystem.repository.AppointmentServiceTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
@Validated
public class ServiceController {

    private final AppointmentServiceTypeRepository serviceRepository;

    @Autowired
    public ServiceController(AppointmentServiceTypeRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    // Endpoint pobierający wszystkie usługi
    @GetMapping
    public ResponseEntity<List<AppointmentServiceType>> getAllServices() {
        List<AppointmentServiceType> services = serviceRepository.findAll();
        return ResponseEntity.ok(services);
    }

    // Endpoint dodający nową usługę
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addNewService(@Valid @RequestBody AppointmentServiceType newService) {
        Map<String, Object> response = new HashMap<>();

        if (newService.getPrice() <= 0) {
            response.put("error", "Cena usługi musi być większa od zera.");
            return ResponseEntity.badRequest().body(response);
        }

        AppointmentServiceType savedService = serviceRepository.save(newService);

        response.put("message", "Service added successfully");
        response.put("service", savedService);
        return ResponseEntity.ok(response);
    }
}
