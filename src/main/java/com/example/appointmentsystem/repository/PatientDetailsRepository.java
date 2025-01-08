package com.example.appointmentsystem.repository;

import com.example.appointmentsystem.model.PatientDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientDetailsRepository extends JpaRepository<PatientDetails, Long> {
    Optional<PatientDetails> findByAvailabilityId(Long availabilityId);
    void deleteByAvailabilityId(Long availabilityId);
}
