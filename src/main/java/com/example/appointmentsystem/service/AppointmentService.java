package com.example.appointmentsystem.service;

import com.example.appointmentsystem.model.Appointment;
import com.example.appointmentsystem.model.AppointmentServiceType;
import com.example.appointmentsystem.repository.AppointmentRepository;
import com.example.appointmentsystem.repository.AppointmentServiceTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentServiceTypeRepository appointmentServiceTypeRepository;

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> getAppointmentsByUserId(Long userId) {
        return appointmentRepository.findByUserId(userId);
    }

    public Appointment saveAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    public void updatePaymentStatus(String orderId, boolean isPaid) {
        Appointment appointment = appointmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Appointment not found for order ID: " + orderId));
        appointment.setPaid(isPaid);
        appointmentRepository.save(appointment);
    }

    public AppointmentServiceType findServiceById(Long serviceId) {
        Optional<AppointmentServiceType> service = appointmentServiceTypeRepository.findById(serviceId);
        return service.orElse(null);
    }

    public void deleteAppointment(Long id, Long userId) {
        Appointment appointment = appointmentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointmentRepository.delete(appointment);
    }
}
