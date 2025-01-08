package com.example.appointmentsystem.service;

import com.example.appointmentsystem.model.PatientDetails;
import com.example.appointmentsystem.dto.PatientDetailsDto;
import org.springframework.stereotype.Service;

@Service
public class PatientDetailsMapper {

    public PatientDetailsDto mapToDto(PatientDetails patientDetails) {
        if (patientDetails == null) {
            return null;
        }
        PatientDetailsDto dto = new PatientDetailsDto();
        dto.setAvailabilityId(patientDetails.getAvailability() != null ? patientDetails.getAvailability().getId() : null);
        dto.setFirstName(patientDetails.getFirstName());
        dto.setLastName(patientDetails.getLastName());
        dto.setPesel(patientDetails.getPesel());
        dto.setGender(patientDetails.getGender());
        dto.setBirthDate(patientDetails.getBirthDate());
        dto.setSymptoms(patientDetails.getSymptoms());
        return dto;
    }
}
