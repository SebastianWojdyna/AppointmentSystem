package com.example.appointmentsystem.controller;

import com.example.appointmentsystem.dto.AvailabilityDto;
import com.example.appointmentsystem.dto.AvailabilityRequest;
import com.example.appointmentsystem.dto.PatientDetailsDto;
import com.example.appointmentsystem.model.Availability;
import com.example.appointmentsystem.model.Doctor;
import com.example.appointmentsystem.model.AppointmentServiceType;
import com.example.appointmentsystem.model.User;
import com.example.appointmentsystem.model.PatientDetails;
import com.example.appointmentsystem.repository.AvailabilityRepository;
import com.example.appointmentsystem.service.AvailabilityService;
import com.example.appointmentsystem.service.DoctorService;
import com.example.appointmentsystem.service.ServiceService;
import com.example.appointmentsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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
        String username = authentication.getName();
        User doctorUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Doctor doctor = doctorService.findByUserId(doctorUser.getId())
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        AppointmentServiceType service = serviceService.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        availabilityService.addDoctorAvailability(doctor, service, request.getAvailableTimes(), request.getPrice());
        return ResponseEntity.ok("Availability added successfully");
    }

    @Transactional
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAvailability(@PathVariable Long id, @RequestBody AvailabilityRequest request) {
        Availability availability = availabilityService.findById(id)
                .orElseThrow(() -> new RuntimeException("Availability not found"));

        AppointmentServiceType service = serviceService.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        availability.setService(service);
        availability.setAvailableTime(request.getAvailableTime());
        availability.setPrice(request.getPrice());
        availabilityService.save(availability);

        return ResponseEntity.ok("Availability updated successfully");
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteAvailability(@PathVariable Long id) {
        if (!availabilityRepository.existsById(id)) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Dostępność nie istnieje"));
        }
        availabilityRepository.deleteById(id);
        return ResponseEntity.ok(Collections.singletonMap("message", "Dostępność została usunięta"));
    }

    // Rezerwacja dostępności przez pacjenta
    @PostMapping("/book/{id}")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public ResponseEntity<?> bookAppointment(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        User patient = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        availabilityService.bookAppointment(id, patient);
        return ResponseEntity.ok(Collections.singletonMap("message", "Wizyta została zarezerwowana"));
    }

    // Rezerwacja z danymi pacjenta (ankieta)
    @PostMapping("/book-with-details")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public ResponseEntity<?> bookAppointmentWithDetails(
            @RequestBody PatientDetailsDto detailsDto,
            Authentication authentication) {

        String username = authentication.getName();
        User patient = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Availability availability = availabilityService.findById(detailsDto.getAvailabilityId())
                .orElseThrow(() -> new RuntimeException("Availability not found"));

        if (availability.getIsBooked()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Appointment is already booked."));
        }

        // Tworzenie obiektu PatientDetails
        PatientDetails details = new PatientDetails();
        details.setFirstName(detailsDto.getFirstName());
        details.setLastName(detailsDto.getLastName());
        details.setPesel(detailsDto.getPesel());
        details.setGender(detailsDto.getGender());
        details.setBirthDate(detailsDto.getBirthDate());
        details.setSymptoms(detailsDto.getSymptoms());

        availabilityService.bookAppointment(detailsDto.getAvailabilityId(), patient, details);

        return ResponseEntity.ok(Collections.singletonMap("message", "Wizyta została zarezerwowana z danymi pacjenta."));
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

    // Pobranie wszystkich dostępnych wizyt (dla pacjenta)
    @GetMapping
    public ResponseEntity<List<Availability>> getAllAvailability() {
        return ResponseEntity.ok(availabilityService.getAllAvailability());
    }

    @GetMapping("/reserved")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public ResponseEntity<List<AvailabilityDto>> getReservedAppointments(Authentication authentication) {
        String username = authentication.getName();
        User patient = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        List<AvailabilityDto> reservedAppointments = availabilityService.getReservedAppointments(patient.getId());
        return ResponseEntity.ok(reservedAppointments);
    }

    @DeleteMapping("/cancel/{id}")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        User patient = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Patient not found!"));

        Availability availability = availabilityService.findById(id)
                .orElseThrow(() -> new RuntimeException("Availability not found"));

        // Usuń powiązane dane pacjenta, jeśli istnieją
        if (availability.getPatientDetails() != null) {
            availabilityService.deletePatientDetails(availability.getPatientDetails());
            availability.setPatientDetails(null);
        }

        availabilityService.cancelAppointment(id, patient);
        return ResponseEntity.ok(Collections.singletonMap("message", "Rezerwacja została anulowana!"));
    }


    @GetMapping("/filter")
    public ResponseEntity<List<Availability>> filterAvailability(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String specialization) {

        List<Availability> availabilities = availabilityService.filterAvailability(date, specialization);
        return ResponseEntity.ok(availabilities);
    }

    @PutMapping("/patient-details/{availabilityId}")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public ResponseEntity<?> updatePatientDetails(@PathVariable Long availabilityId, @RequestBody PatientDetailsDto detailsDto) {
        Availability availability = availabilityService.findById(availabilityId)
                .orElseThrow(() -> new RuntimeException("Availability not found"));

        if (!availability.getIsBooked()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Appointment is not booked."));
        }

        PatientDetails patientDetails = availability.getPatientDetails();
        if (patientDetails == null) {
            patientDetails = new PatientDetails();
            patientDetails.setAvailability(availability);
        }

        patientDetails.setFirstName(detailsDto.getFirstName());
        patientDetails.setLastName(detailsDto.getLastName());
        patientDetails.setPesel(detailsDto.getPesel());
        patientDetails.setGender(detailsDto.getGender());
        patientDetails.setBirthDate(detailsDto.getBirthDate());
        patientDetails.setSymptoms(detailsDto.getSymptoms());

        availabilityService.savePatientDetails(patientDetails);

        return ResponseEntity.ok(Collections.singletonMap("message", "Patient details updated successfully."));
    }

}
