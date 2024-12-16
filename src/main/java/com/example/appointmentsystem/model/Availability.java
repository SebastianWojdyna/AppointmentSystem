package com.example.appointmentsystem.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Availability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private AppointmentServiceType service;

    private LocalDateTime availableTime;
    private double price;

    private String specialization; // Dodane pole na specjalizację

    // Gettery i Settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public AppointmentServiceType getService() { return service; }
    public void setService(AppointmentServiceType service) { this.service = service; }

    public LocalDateTime getAvailableTime() { return availableTime; }
    public void setAvailableTime(LocalDateTime availableTime) { this.availableTime = availableTime; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
}
