package com.example.appointmentsystem.repository;

import com.example.appointmentsystem.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Optional<Appointment> findByOrderId(String orderId);
    List<Appointment> findByUserId(Long userId);
    Optional<Appointment> findByIdAndUserId(Long id, Long userId);  // Metoda do wyszukiwania wizyt po ID i ID użytkownika
}
