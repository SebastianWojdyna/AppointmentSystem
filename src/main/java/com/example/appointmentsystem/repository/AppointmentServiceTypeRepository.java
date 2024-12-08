package com.example.appointmentsystem.repository;

import com.example.appointmentsystem.model.AppointmentServiceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentServiceTypeRepository extends JpaRepository<AppointmentServiceType, Long> {
}
