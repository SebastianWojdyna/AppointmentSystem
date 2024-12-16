package com.example.appointmentsystem.service;

import com.example.appointmentsystem.model.Availability;
import com.example.appointmentsystem.model.Doctor;
import com.example.appointmentsystem.model.AppointmentServiceType;
import com.example.appointmentsystem.repository.AvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AvailabilityService {

    @Autowired
    private AvailabilityRepository availabilityRepository;

    // Dodanie nowej dostępności przez lekarza
    public void addDoctorAvailability(Doctor doctor, AppointmentServiceType service, List<LocalDateTime> times, double price) {
        String specialization = doctor.getSpecialization(); // Pobranie specjalizacji lekarza

        times.forEach(time -> {
            Availability availability = new Availability();
            availability.setDoctor(doctor);
            availability.setService(service);
            availability.setAvailableTime(time);
            availability.setPrice(price);
            availability.setSpecialization(specialization); // Dodanie specjalizacji
            availabilityRepository.save(availability);
        });
    }


    // Pobranie dostępności lekarza
    public List<Availability> getDoctorAvailability(Long doctorId) {
        return availabilityRepository.findByDoctorId(doctorId);
    }

    // Pobranie dostępności dla usługi
    public List<Availability> getServiceAvailability(Long serviceId) {
        return availabilityRepository.findByServiceId(serviceId);
    }
}
