package com.example.appointmentsystem.controller;

import com.example.appointmentsystem.model.AppointmentServiceType;
import com.example.appointmentsystem.repository.AppointmentServiceTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/services")
@Validated
public class ServiceController {

    private final AppointmentServiceTypeRepository serviceRepository;

    @Autowired
    public ServiceController(AppointmentServiceTypeRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @GetMapping
    public ResponseEntity<List<AppointmentServiceType>> getAllServices() {
        List<AppointmentServiceType> services = serviceRepository.findAll();
        return ResponseEntity.ok(services);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addNewService(@Valid @RequestBody AppointmentServiceType newService) {
        if (newService.getPrice() <= 0) {
            return ResponseEntity.badRequest().body("Cena usługi musi być większa od zera.");
        }

        AppointmentServiceType savedService = serviceRepository.save(newService);
        return ResponseEntity.ok(savedService);
    }
}
