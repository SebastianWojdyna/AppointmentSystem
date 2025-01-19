package com.example.appointmentsystem.service;

import com.example.appointmentsystem.model.Availability;
import com.example.appointmentsystem.model.Doctor;
import com.example.appointmentsystem.model.AppointmentServiceType;
import com.example.appointmentsystem.model.User;
import com.example.appointmentsystem.model.PatientDetails;
import com.example.appointmentsystem.dto.AvailabilityDto;
import com.example.appointmentsystem.repository.AvailabilityRepository;
import com.example.appointmentsystem.repository.DoctorRepository;
import com.example.appointmentsystem.repository.PatientDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private PatientDetailsRepository patientDetailsRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    private final PatientDetailsMapper patientDetailsMapper;


    private static final Logger logger = LoggerFactory.getLogger(AvailabilityService.class);

    // Konstruktor dla wstrzyknięcia PatientDetailsMapper
    public AvailabilityService(PatientDetailsMapper patientDetailsMapper) {
        this.patientDetailsMapper = patientDetailsMapper;
    }

    public void addDoctorAvailability(Doctor doctor, AppointmentServiceType service, List<LocalDateTime> times, double price) {
        String specialization = doctor.getSpecialization();
        times.forEach(time -> {
            Availability availability = new Availability();
            availability.setDoctor(doctor);
            availability.setService(service);
            availability.setAvailableTime(time);
            availability.setPrice(price);
            availability.setSpecialization(specialization);
            availabilityRepository.save(availability);
        });
    }

    public Optional<Availability> findById(Long id) {
        return availabilityRepository.findById(id);
    }


    public Availability save(Availability availability) {
        return availabilityRepository.save(availability);
    }

    public List<Availability> getDoctorAvailability(Long doctorId) {
        return availabilityRepository.findByDoctorId(doctorId);
    }

    public List<Availability> getServiceAvailability(Long serviceId) {
        return availabilityRepository.findByServiceId(serviceId);
    }

    public List<Availability> getAllAvailability() {
        return availabilityRepository.findAll();
    }

    // Rezerwacja wizyty
    @Transactional
    public void bookAppointment(Long availabilityId, User patient, PatientDetails details) {
        Availability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new RuntimeException("Availability not found"));

        if (availability.getIsBooked()) {
            throw new RuntimeException("This appointment is already booked");
        }

        // Sprawdzenie, czy dane pacjenta są kompletne
        validatePatientDetails(details);

        availability.setIsBooked(true);
        availability.setPatient(patient);

        // Przypisanie dostępności do danych pacjenta
        details.setAvailability(availability);
        patientDetailsRepository.save(details);

        // Zapis dostępności z przypisanym pacjentem
        availabilityRepository.save(availability);
    }


    // Rezerwacja wizyty bez ankiety (tylko dla istniejących pacjentów)
    public void bookAppointment(Long availabilityId, User patient) {
        Availability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new RuntimeException("Availability not found"));

        if (availability.getIsBooked()) {
            throw new RuntimeException("This appointment is already booked");
        }

        availability.setIsBooked(true);
        availability.setPatient(patient);
        availabilityRepository.save(availability);
    }

    public List<AvailabilityDto> getReservedAppointments(Long patientId) {
        List<Availability> appointments = availabilityRepository.findByPatientId(patientId);
        return appointments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Mapowanie Availability na AvailabilityDto
    private AvailabilityDto mapToDto(Availability availability) {
        AvailabilityDto dto = new AvailabilityDto();
        dto.setId(availability.getId());
        dto.setDoctor(availability.getDoctor());
        dto.setService(availability.getService());
        dto.setAvailableTime(availability.getAvailableTime());
        dto.setPrice(availability.getPrice());
        dto.setSpecialization(availability.getSpecialization());
        dto.setIsBooked(availability.getIsBooked());

        if (availability.getPatientDetails() != null) {
            dto.setPatientDetails(patientDetailsMapper.mapToDto(availability.getPatientDetails()));
        }

        return dto;
    }

    @Transactional
    public void cancelAppointment(Long availabilityId, User patient) {
        Availability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new RuntimeException("Availability not found"));

        if (!availability.getIsBooked() || !availability.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Nie możesz anulować tej rezerwacji.");
        }

        // Usunięcie powiązanych danych pacjenta
        patientDetailsRepository.deleteByAvailabilityId(availability.getId());

        availability.setIsBooked(false);
        availability.setPatient(null);
        availabilityRepository.save(availability);
    }

    public List<Availability> filterAvailability(String date, String specialization) {
        return availabilityRepository.findAll().stream()
                .filter(a -> (date == null || a.getAvailableTime().toLocalDate().toString().equals(date)))
                .filter(a -> (specialization == null || a.getSpecialization().equalsIgnoreCase(specialization)))
                .collect(Collectors.toList());
    }

    private void validatePatientDetails(PatientDetails details) {
        if (details.getFirstName() == null || details.getFirstName().isEmpty()) {
            throw new RuntimeException("First name is required");
        }
        if (details.getLastName() == null || details.getLastName().isEmpty()) {
            throw new RuntimeException("Last name is required");
        }
        if (details.getPesel() == null || details.getPesel().isEmpty()) {
            throw new RuntimeException("PESEL is required");
        }
        if (details.getGender() == null || details.getGender().isEmpty()) {
            throw new RuntimeException("Gender is required");
        }
        if (details.getBirthDate() == null) {
            throw new RuntimeException("Birth date is required");
        }
    }

    public void savePatientDetails(PatientDetails patientDetails) {
        patientDetailsRepository.save(patientDetails);
    }

    @Transactional
    public void deletePatientDetails(PatientDetails patientDetails) {
        if (patientDetails != null) {
            patientDetailsRepository.delete(patientDetails);
        }
    }

    // ### Rekomendacje wizyt ###
    private static final int DEFAULT_MAX_DAYS = 7;
    private static final int MAX_RESULTS = 5;

    public List<AvailabilityDto> getRecommendations(String date, String specialization, Long doctorId) {
        logger.info("Rozpoczynanie generowania rekomendacji...");
        logger.info("Podane kryteria - Data: {}, Specjalizacja: {}, ID Lekarza: {}", date, specialization, doctorId);

        final String finalSpecialization = resolveSpecialization(specialization, doctorId);
        final String finalDate = date;
        final Long finalDoctorId = doctorId;

        List<Availability> allAppointments = availabilityRepository.findAll();
        logger.info("Pobrano {} wizyt z bazy danych.", allAppointments.size());

        List<AvailabilityDto> recommendations = new ArrayList<>();

        // 1. Priorytet: Dokładne dopasowanie wszystkich kryteriów
        List<Availability> exactMatches = allAppointments.stream()
                .filter(a -> !a.getIsBooked())
                .filter(a -> finalDate == null || a.getAvailableTime().toLocalDate().toString().equals(finalDate))
                .filter(a -> finalSpecialization == null || a.getSpecialization().equalsIgnoreCase(finalSpecialization))
                .filter(a -> finalDoctorId == null || a.getDoctor().getId().equals(finalDoctorId))
                .sorted(Comparator.comparing(Availability::getAvailableTime))
                .limit(MAX_RESULTS)
                .collect(Collectors.toList());
        addRecommendations(recommendations, exactMatches, "Dokładne dopasowanie wszystkich kryteriów");

        // 2. Priorytet: Najbliższe terminy tego samego lekarza
        if (finalDoctorId != null) {
            List<Availability> sameDoctorMatches = findNearestAppointments(allAppointments, finalDate, finalDoctorId, null);
            addRecommendations(recommendations, sameDoctorMatches, "Najbliższe terminy wybranego lekarza (+/- " + DEFAULT_MAX_DAYS + " dni)");
        }

        // 3. Priorytet: Inny lekarz tej samej specjalizacji w tej samej dacie
        if (finalDate != null && finalSpecialization != null) {
            List<Availability> sameSpecializationSameDate = allAppointments.stream()
                    .filter(a -> !a.getIsBooked())
                    .filter(a -> a.getAvailableTime().toLocalDate().toString().equals(finalDate))
                    .filter(a -> a.getSpecialization().equalsIgnoreCase(finalSpecialization))
                    .sorted(Comparator.comparing(Availability::getAvailableTime))
                    .limit(MAX_RESULTS)
                    .collect(Collectors.toList());
            addRecommendations(recommendations, sameSpecializationSameDate, "Inny lekarz tej samej specjalizacji w tej samej dacie");
        }

        // 4. Priorytet: Lekarze tej samej specjalizacji w najbliższych terminach
        if (finalSpecialization != null) {
            List<Availability> sameSpecializationNearest = findNearestAppointments(allAppointments, null, null, finalSpecialization);
            addRecommendations(recommendations, sameSpecializationNearest, "Lekarze tej samej specjalizacji w najbliższych terminach (+/- " + DEFAULT_MAX_DAYS + " dni)");
        }

        // 5. Priorytet: Lekarze pierwszego kontaktu w tej samej dacie lub najbliższej dostępnej (+/- 7 dni)
        if (finalDate != null) {
            List<Availability> generalPractitioners = allAppointments.stream()
                    .filter(a -> !a.getIsBooked())
                    .filter(a -> a.getSpecialization().equalsIgnoreCase("internista") || a.getSpecialization().equalsIgnoreCase("poz"))
                    .filter(a -> isWithinDateRange(a, LocalDate.parse(finalDate), DEFAULT_MAX_DAYS))
                    .sorted(Comparator.comparing(Availability::getAvailableTime))
                    .limit(MAX_RESULTS)
                    .collect(Collectors.toList());
            addRecommendations(recommendations, generalPractitioners, "Lekarze pierwszego kontaktu w tej samej dacie lub najbliższych dostępnych (+/- " + DEFAULT_MAX_DAYS + " dni)");
        }

        // 6. Priorytet: Wszystkie dostępne terminy w wybranej dacie (+/- 7 dni), jeśli brak specjalizacji i lekarza
        if (finalDate != null && (finalSpecialization == null || finalSpecialization.isEmpty()) && finalDoctorId == null) {
            List<Availability> allDoctorsMatches = allAppointments.stream()
                    .filter(a -> !a.getIsBooked())
                    .filter(a -> isWithinDateRange(a, LocalDate.parse(finalDate), DEFAULT_MAX_DAYS))
                    .sorted(Comparator.comparing(Availability::getAvailableTime))
                    .limit(MAX_RESULTS)
                    .collect(Collectors.toList());
            addRecommendations(recommendations, allDoctorsMatches, "Dostępne wizyty w wybranej dacie (+/- " + DEFAULT_MAX_DAYS + " dni)");
        }

        logger.info("Znaleziono {} rekomendacji w sumie.", recommendations.size());
        return recommendations;
    }

    private String resolveSpecialization(String specialization, Long doctorId) {
        if (specialization == null || specialization.isEmpty()) {
            if (doctorId != null) {
                Optional<Doctor> doctor = doctorRepository.findById(doctorId);
                if (doctor.isPresent()) {
                    return doctor.get().getSpecialization();
                }
            }
        }
        return specialization;
    }

    private void addRecommendations(List<AvailabilityDto> recommendations, List<Availability> matches, String description) {
        matches.stream()
                .map(this::mapToDto)
                .peek(dto -> dto.setDescription(description))
                .forEach(recommendations::add);
    }

    private List<Availability> findNearestAppointments(List<Availability> allAppointments, String date, Long doctorId, String specialization) {
        List<Availability> results = new ArrayList<>();
        LocalDate targetDate = (date != null && !date.isEmpty()) ? LocalDate.parse(date) : null;

        for (int range = 1; range <= DEFAULT_MAX_DAYS; range++) {
            int currentRange = range;
            results = allAppointments.stream()
                    .filter(a -> !a.getIsBooked())
                    .filter(a -> doctorId == null || a.getDoctor().getId().equals(doctorId))
                    .filter(a -> specialization == null || a.getSpecialization().equalsIgnoreCase(specialization))
                    .filter(a -> isWithinDateRange(a, targetDate, currentRange))
                    .sorted(Comparator.comparing(Availability::getAvailableTime))
                    .limit(MAX_RESULTS)
                    .collect(Collectors.toList());
            if (!results.isEmpty()) break;
        }

        return results;
    }

    private boolean isWithinDateRange(Availability availability, LocalDate targetDate, int range) {
        if (targetDate != null) {
            LocalDate appointmentDate = availability.getAvailableTime().toLocalDate();
            return !appointmentDate.isBefore(targetDate.minusDays(range)) &&
                    !appointmentDate.isAfter(targetDate.plusDays(range));
        }
        return false;
    }
}
