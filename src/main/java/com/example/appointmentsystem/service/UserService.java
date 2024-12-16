package com.example.appointmentsystem.service;

import com.example.appointmentsystem.model.Doctor;
import com.example.appointmentsystem.model.Role;
import com.example.appointmentsystem.model.User;
import com.example.appointmentsystem.repository.DoctorRepository;
import com.example.appointmentsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Rejestracja użytkownika z obsługą specjalizacji dla lekarzy
    public User registerUser(User user, String specialization) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        if (savedUser.getRole() == Role.DOCTOR) {
            Doctor doctor = new Doctor();
            doctor.setUser(savedUser);
            doctor.setName(savedUser.getUsername());
            doctor.setSpecialization(specialization != null && !specialization.trim().isEmpty()
                    ? specialization
                    : "General");
            doctorRepository.save(doctor);
        }

        return savedUser;
    }

    // Pobranie użytkownika po emailu
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Pobranie użytkownika po ID
    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // Pobranie listy użytkowników z paginacją
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    // Aktualizacja użytkownika
    public User updateUserWithSpecialization(Long id, User updatedUser, String specialization) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setUsername(updatedUser.getUsername());
        user.setEmail(updatedUser.getEmail());
        user.setRole(updatedUser.getRole());
        User savedUser = userRepository.save(user);

        if (updatedUser.getRole() == Role.DOCTOR) {
            Doctor doctor = doctorRepository.findByUserId(savedUser.getId())
                    .orElseGet(() -> {
                        Doctor newDoctor = new Doctor();
                        newDoctor.setUser(savedUser);
                        return newDoctor;
                    });

            doctor.setSpecialization(specialization != null && !specialization.trim().isEmpty()
                    ? specialization
                    : "General");
            doctor.setName(savedUser.getUsername());
            doctorRepository.save(doctor);
        }

        return savedUser;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }


}
