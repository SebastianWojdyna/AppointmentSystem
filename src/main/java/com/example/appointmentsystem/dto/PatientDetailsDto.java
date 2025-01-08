package com.example.appointmentsystem.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

public class PatientDetailsDto {

    private Long availabilityId;

    @NotBlank(message = "Imię jest wymagane.")
    private String firstName;

    @NotBlank(message = "Nazwisko jest wymagane.")
    private String lastName;

    @Pattern(regexp = "\\d{11}", message = "PESEL musi składać się z 11 cyfr.")
    private String pesel;

    @NotBlank(message = "Płeć jest wymagana.")
    private String gender;

    private LocalDate birthDate;

    @Size(max = 1000, message = "Opis objawów nie może przekraczać 1000 znaków.")
    private String symptoms;

    // Gettery i settery
    public Long getAvailabilityId() { return availabilityId; }
    public void setAvailabilityId(Long availabilityId) { this.availabilityId = availabilityId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPesel() { return pesel; }
    public void setPesel(String pesel) { this.pesel = pesel; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
}
