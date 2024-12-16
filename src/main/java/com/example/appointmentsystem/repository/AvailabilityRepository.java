package com.example.appointmentsystem.repository;

import com.example.appointmentsystem.model.Availability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByDoctorId(Long doctorId);
    List<Availability> findByServiceId(Long serviceId);
    List<Availability> findAll();
    List<Availability> findByPatientId(Long patientId);


}
