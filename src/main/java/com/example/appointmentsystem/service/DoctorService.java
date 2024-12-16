package com.example.appointmentsystem.service;

import com.example.appointmentsystem.model.Doctor;
import com.example.appointmentsystem.model.User;
import com.example.appointmentsystem.repository.DoctorRepository;
import com.example.appointmentsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Pobiera wszystkich lekarzy.
     */
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    /**
     * Znajduje lekarza po ID.
     */
    public Doctor findDoctorById(Long id) {
        return doctorRepository.findById(id).orElse(null);
    }

    /**
     * Pobiera lekarzy po ID usługi.
     */
    public List<Doctor> getDoctorsByService(Long serviceId) {
        return doctorRepository.findByServices_Id(serviceId);
    }

    /**
     * Pobiera unikalne specjalizacje lekarzy.
     */
    public List<String> getAllSpecializations() {
        return doctorRepository.findAll()
                .stream()
                .map(Doctor::getSpecialization)
                .filter(specialization -> specialization != null && !specialization.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Pobiera lekarzy po specjalizacji.
     */
    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }

    /**
     * Znajduje lekarza na podstawie emaila użytkownika.
     */
    public Optional<Doctor> findByUserEmail(String email) {
        return doctorRepository.findByUser_Email(email);
    }

    /**
     * Tworzy lub pobiera profil lekarza na podstawie zalogowanego użytkownika.
     */
    public Doctor getOrCreateDoctorProfile(User user) {
        Optional<Doctor> doctorOptional = doctorRepository.findByUserId(user.getId());

        if (doctorOptional.isEmpty()) {
            // Tworzenie nowego profilu lekarza, jeśli nie istnieje
            Doctor newDoctor = new Doctor();
            newDoctor.setName(user.getUsername());
            newDoctor.setSpecialization("Not Specified"); // Domyślna wartość
            newDoctor.setUser(user);
            return doctorRepository.save(newDoctor);
        }

        // Zwrócenie istniejącego profilu
        return doctorOptional.get(); // Zwrócenie istniejącego obiektu Doctor
    }

    /**
     * Zapisuje lekarza do bazy danych.
     */
    public Doctor saveDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    /**
     * Znajduje lekarza po ID.
     */
    public Optional<Doctor> getDoctorById(Long id) {
        return doctorRepository.findById(id);
    }

    private Optional<Doctor> createDefaultDoctorProfile(String email) {
        // Pobierz użytkownika na podstawie emaila
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Utwórz domyślny profil doktora
            Doctor newDoctor = new Doctor();
            newDoctor.setName("Dr. " + user.getUsername());
            newDoctor.setSpecialization("General Medicine");
            newDoctor.setUser(user);

            doctorRepository.save(newDoctor);
            return Optional.of(newDoctor);
        }
        return Optional.empty();
    }

    public Optional<Doctor> findByUserId(Long userId) {
        return doctorRepository.findByUserId(userId);
    }
}
