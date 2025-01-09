package com.example.appointmentsystem.dto;

import com.example.appointmentsystem.model.Doctor;
import com.example.appointmentsystem.model.AppointmentServiceType;

import java.time.LocalDateTime;

public class AvailabilityDto {

    private Long id;
    private Doctor doctor;
    private AppointmentServiceType service;
    private LocalDateTime availableTime;
    private double price;
    private String specialization;
    private boolean isBooked;
    private PatientDetailsDto patientDetails;

    private String description;

    // Gettery i settery
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public AppointmentServiceType getService() {
        return service;
    }

    public void setService(AppointmentServiceType service) {
        this.service = service;
    }

    public LocalDateTime getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(LocalDateTime availableTime) {
        this.availableTime = availableTime;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public boolean getIsBooked() {
        return isBooked;
    }

    public void setIsBooked(boolean isBooked) {
        this.isBooked = isBooked;
    }

    public PatientDetailsDto getPatientDetails() {
        return patientDetails;
    }

    public void setPatientDetails(PatientDetailsDto patientDetails) {
        this.patientDetails = patientDetails;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
