package com.example.appointmentsystem.dto;

public class UpdateUserRoleRequest {
    private String role;
    private String specialization;

    // Gettery i Settery
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}
