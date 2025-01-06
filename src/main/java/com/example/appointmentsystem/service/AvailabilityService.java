package com.example.appointmentsystem.service;

import com.example.appointmentsystem.model.Availability;
import com.example.appointmentsystem.model.Doctor;
import com.example.appointmentsystem.model.AppointmentServiceType;
import com.example.appointmentsystem.model.User;
import com.example.appointmentsystem.repository.AvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    @Autowired
    private AvailabilityRepository availabilityRepository;

    public void addDoctorAvailability(Doctor doctor, AppointmentServiceType service, List<LocalDateTime> times, double price) {
        String specialization = doctor.getSpecialization();
        times.forEach(time -> {
            Availability availability = new Availability();
            availability.setDoctor(doctor);
            availability.setService(service);
            availability.setAvailableTime(time);
            availability.setPrice(price);
            availability.setSpecialization(specialization);
            availabilityRepository.save(availability);
        });
    }

    public Optional<Availability> findById(Long id) {
        return availabilityRepository.findById(id);
    }


    public Availability save(Availability availability) {
        return availabilityRepository.save(availability);
    }

    public List<Availability> getDoctorAvailability(Long doctorId) {
        return availabilityRepository.findByDoctorId(doctorId);
    }

    public List<Availability> getServiceAvailability(Long serviceId) {
        return availabilityRepository.findByServiceId(serviceId);
    }

    public List<Availability> getAllAvailability() {
        return availabilityRepository.findAll();
    }

    // Rezerwacja wizyty
    public void bookAppointment(Long availabilityId, User patient) {
        Availability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new RuntimeException("Availability not found"));

        if (availability.getIsBooked()) {
            throw new RuntimeException("This appointment is already booked");
        }

        availability.setIsBooked(true);
        availability.setPatient(patient);
        availabilityRepository.save(availability);
    }

    public List<Availability> getReservedAppointments(Long patientId) {
        return availabilityRepository.findByPatientId(patientId);
    }

    public void cancelAppointment(Long availabilityId, User patient){
        Availability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new RuntimeException("Availability not found"));

        if (!availability.getIsBooked() || !availability.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Nie możesz anulować tej rezerwacji.");
        }

        availability.setIsBooked(false);
        availability.setPatient(null);
        availabilityRepository.save(availability);
    }

    public List<Availability> filterAvailability(String date, String specialization) {
        return availabilityRepository.findAll().stream()
                .filter(a -> (date == null || a.getAvailableTime().toLocalDate().toString().equals(date)))
                .filter(a -> (specialization == null || a.getSpecialization().equalsIgnoreCase(specialization)))
                .collect(Collectors.toList());
    }

}
