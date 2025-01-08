package com.example.appointmentsystem.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;

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

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = true)
    private User patient;

    private LocalDateTime availableTime;
    private double price;

    private String specialization;

    @Column(nullable = false)
    private boolean isBooked = false;

    @OneToOne(mappedBy = "availability", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private PatientDetails patientDetails;


    @PrePersist
    @PreUpdate
    private void syncPriceWithService() {
        if (service != null) {
            this.price = service.getPrice();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public AppointmentServiceType getService() { return service; }
    public void setService(AppointmentServiceType service) {
        this.service = service;
        if (service != null) {
            this.price = service.getPrice();
        }
    }

    public LocalDateTime getAvailableTime() { return availableTime; }
    public void setAvailableTime(LocalDateTime availableTime) { this.availableTime = availableTime; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public User getPatient() { return patient; }
    public void setPatient(User patient) { this.patient = patient; }

    public boolean getIsBooked() { return isBooked; }
    public void setIsBooked(boolean isBooked) { this.isBooked = isBooked; }

    public PatientDetails getPatientDetails() { return patientDetails; }
    public void setPatientDetails(PatientDetails patientDetails) { this.patientDetails = patientDetails; }
}
