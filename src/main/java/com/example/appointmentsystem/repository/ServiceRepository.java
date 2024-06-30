package com.example.appointmentsystem.repository;

import com.example.appointmentsystem.model.AppointmentServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<AppointmentServiceType, Long> {
}
