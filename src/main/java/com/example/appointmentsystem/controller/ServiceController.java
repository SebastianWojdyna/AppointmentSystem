package com.example.appointmentsystem.controller;

import com.example.appointmentsystem.model.AppointmentServiceType;
import com.example.appointmentsystem.repository.AppointmentServiceTypeRepository;
import com.example.appointmentsystem.repository.AvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/services")
@Validated
public class ServiceController {

    private final AppointmentServiceTypeRepository serviceRepository;
    private final AvailabilityRepository availabilityRepository;

    @Autowired
    public ServiceController(AppointmentServiceTypeRepository serviceRepository,
                             AvailabilityRepository availabilityRepository) {
        this.serviceRepository = serviceRepository;
        this.availabilityRepository = availabilityRepository;
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

    // Endpoint usuwający usługę
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteService(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        Optional<AppointmentServiceType> service = serviceRepository.findById(id);

        if (service.isPresent()) {
            // Sprawdzenie, czy istnieją dostępności powiązane z tą usługą
            if (!availabilityRepository.findByServiceId(id).isEmpty()) {
                response.put("error", "Nie można usunąć usługi, ponieważ są z nią powiązane dostępności.");
                return ResponseEntity.badRequest().body(response);
            }

            serviceRepository.deleteById(id);
            response.put("message", "Usługa została pomyślnie usunięta.");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Usługa nie została znaleziona.");
            return ResponseEntity.status(404).body(response);
        }
    }
}
