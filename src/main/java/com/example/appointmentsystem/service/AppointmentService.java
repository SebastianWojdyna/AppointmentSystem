package com.example.appointmentsystem.service;

import com.example.appointmentsystem.model.Appointment;
import com.example.appointmentsystem.model.AppointmentServiceType;
import com.example.appointmentsystem.model.Doctor;
import com.example.appointmentsystem.model.User;
import com.example.appointmentsystem.repository.AppointmentRepository;
import com.example.appointmentsystem.repository.AppointmentServiceTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentServiceTypeRepository appointmentServiceTypeRepository;

    // Dodawanie dostępności przez lekarza
    public void addDoctorAvailability(Doctor doctor, List<LocalDateTime> availableTimes) {
        List<Appointment> appointments = availableTimes.stream().map(time -> {
            Appointment appointment = new Appointment();
            appointment.setDoctor(doctor);
            appointment.setAppointmentTime(time);
            appointment.setAvailable(true);
            return appointment;
        }).collect(Collectors.toList());
        appointmentRepository.saveAll(appointments);
    }

    // Pobieranie dostępnych terminów lekarza
    public List<Appointment> getAvailableAppointmentsByDoctorId(Long doctorId) {
        return appointmentRepository.findByDoctorIdAndAvailableTrue(doctorId);
    }

    // Rezerwacja terminu przez pacjenta
    public void bookAppointment(Long appointmentId, User user) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        if (!appointment.isAvailable()) {
            throw new RuntimeException("Appointment is already booked");
        }
        appointment.setAvailable(false);
        appointment.setUser(user);
        appointment.setPatientName(user.getUsername());
        appointmentRepository.save(appointment);
    }

    // Znajdowanie usługi na podstawie ID
    public AppointmentServiceType findServiceById(Long serviceId) {
        Optional<AppointmentServiceType> service = appointmentServiceTypeRepository.findById(serviceId);
        return service.orElseThrow(() -> new RuntimeException("Service not found with ID: " + serviceId));
    }

    // Zapisywanie wizyty
    public Appointment saveAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    // Pobieranie wizyt użytkownika
    public List<Appointment> getAppointmentsByUserId(Long userId) {
        return appointmentRepository.findByUserId(userId);
    }

    // Pobieranie wizyt lekarza
    public List<Appointment> getAppointmentsByDoctorId(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    // Usuwanie wizyty przez pacjenta
    public void deleteAppointment(Long id, Long userId) {
        Appointment appointment = appointmentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointmentRepository.delete(appointment);
    }

    // Pobieranie wszystkich wizyt
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }
}
