package com.example.appointmentsystem.service;

import com.example.appointmentsystem.model.Appointment;
import com.example.appointmentsystem.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
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
}
