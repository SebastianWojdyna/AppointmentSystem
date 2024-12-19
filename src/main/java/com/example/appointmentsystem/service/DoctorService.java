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


    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }


    public Doctor findDoctorById(Long id) {
        return doctorRepository.findById(id).orElse(null);
    }


    public List<Doctor> getDoctorsByService(Long serviceId) {
        return doctorRepository.findByServices_Id(serviceId);
    }


    public List<String> getAllSpecializations() {
        return doctorRepository.findAll()
                .stream()
                .map(Doctor::getSpecialization)
                .filter(specialization -> specialization != null && !specialization.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }


    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }


    public Optional<Doctor> findByUserEmail(String email) {
        return doctorRepository.findByUser_Email(email);
    }


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


    public Doctor saveDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }


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

    public Optional<Doctor> findByUserUsername(String username) {
        return doctorRepository.findByUser_Username(username);
    }
}
