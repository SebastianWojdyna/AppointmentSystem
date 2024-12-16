package com.example.appointmentsystem.repository;

import com.example.appointmentsystem.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findByServices_Id(Long serviceId);
    List<Doctor> findBySpecialization(String specialization);
    boolean existsById(Long id);
    Optional<Doctor> findByUser_Email(String email);
    Optional<Doctor> findByUserId(long userId);
    Optional<Doctor> findByUser_Username(String username);

}
