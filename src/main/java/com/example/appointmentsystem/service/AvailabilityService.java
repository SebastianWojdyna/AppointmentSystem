package com.example.appointmentsystem.service;

import com.example.appointmentsystem.model.Availability;
import com.example.appointmentsystem.model.Doctor;
import com.example.appointmentsystem.model.AppointmentServiceType;
import com.example.appointmentsystem.model.User;
import com.example.appointmentsystem.model.PatientDetails;
import com.example.appointmentsystem.dto.AvailabilityDto;
import com.example.appointmentsystem.repository.AvailabilityRepository;
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

    // Algorytm rekomendacji
    private static final int DEFAULT_MAX_DAYS = 7;
    public List<AvailabilityDto> getRecommendations(String date, String specialization, Long doctorId) {
        logger.info("Rozpoczynanie generowania rekomendacji...");
        logger.info("Podane kryteria - Data: {}, Specjalizacja: {}, ID Lekarza: {}", date, specialization, doctorId);

        List<Availability> allAppointments = availabilityRepository.findAll();
        logger.info("Pobrano {} wizyt z bazy danych.", allAppointments.size());

        List<Availability> recommendedAppointments = new ArrayList<>();

        // Priorytet 1: Dokładne dopasowanie wszystkich kryteriów
        recommendedAppointments = allAppointments.stream()
                .filter(a -> !a.getIsBooked())
                .filter(a -> date == null || a.getAvailableTime().toLocalDate().toString().equals(date))
                .filter(a -> specialization == null || a.getSpecialization().equalsIgnoreCase(specialization))
                .filter(a -> doctorId == null || a.getDoctor().getId().equals(doctorId))
                .sorted(Comparator.comparing(Availability::getAvailableTime))
                .toList();

        logger.info("Priorytet 1: Znaleziono {} wizyt pasujących dokładnie do podanych kryteriów.", recommendedAppointments.size());

        // Priorytet 2: Szukanie tego samego lekarza w najbliższych terminach (do 7 dni w przód i w tył)
        if (recommendedAppointments.isEmpty() && doctorId != null) {
            logger.info("Priorytet 2: Szukanie tego samego lekarza w najbliższych terminach...");
            recommendedAppointments = findNearestAppointments(allAppointments, date, doctorId, null);
            logger.info("Priorytet 2: Znaleziono {} wizyt.", recommendedAppointments.size());
        }

        // Priorytet 3: Szukanie innego lekarza tej samej specjalizacji w tej samej dacie
        if (recommendedAppointments.isEmpty() && date != null && specialization != null) {
            logger.info("Priorytet 3: Szukanie innego lekarza tej samej specjalizacji w tej samej dacie...");
            recommendedAppointments = allAppointments.stream()
                    .filter(a -> !a.getIsBooked())
                    .filter(a -> a.getAvailableTime().toLocalDate().toString().equals(date))
                    .filter(a -> a.getSpecialization().equalsIgnoreCase(specialization))
                    .sorted(Comparator.comparing(Availability::getAvailableTime))
                    .toList();
            logger.info("Priorytet 3: Znaleziono {} wizyt.", recommendedAppointments.size());
        }

        // Priorytet 4: Szukanie lekarza tej samej specjalizacji w innych terminach
        if (recommendedAppointments.isEmpty() && specialization != null) {
            logger.info("Priorytet 4: Szukanie lekarza tej samej specjalizacji w innych terminach...");
            recommendedAppointments = findNearestAppointments(allAppointments, date, null, specialization);
            logger.info("Priorytet 4: Znaleziono {} wizyt.", recommendedAppointments.size());
        }

        // Priorytet 5: Lekarze podstawowej opieki zdrowotnej (internista, POZ)
        if (recommendedAppointments.isEmpty()) {
            logger.info("Priorytet 5: Szukanie wizyt lekarzy podstawowej opieki zdrowotnej...");
            recommendedAppointments = allAppointments.stream()
                    .filter(a -> !a.getIsBooked())
                    .filter(a -> a.getSpecialization().equalsIgnoreCase("internista") || a.getSpecialization().equalsIgnoreCase("poz"))
                    .sorted(Comparator.comparing(Availability::getAvailableTime))
                    .toList();
            logger.info("Priorytet 5: Znaleziono {} wizyt.", recommendedAppointments.size());
        }

        // Jeśli nadal brak wyników
        if (recommendedAppointments.isEmpty()) {
            logger.warn("Nie znaleziono żadnych wizyt spełniających jakiekolwiek kryteria.");
        } else {
            logger.info("Znaleziono {} rekomendacji w sumie.", recommendedAppointments.size());
        }

        return recommendedAppointments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Znajdź najbliższe dostępne wizyty dla podanych kryteriów (data, lekarz, specjalizacja).
     */
    private List<Availability> findNearestAppointments(List<Availability> allAppointments, String date, Long doctorId, String specialization) {
        List<Availability> results = new ArrayList<>();
        try {
            LocalDate targetDate = date != null ? LocalDate.parse(date) : null;

            for (int range = 1; range <= DEFAULT_MAX_DAYS; range++) {
                final int currentRange = range; // Tworzymy finalną zmienną dla lambdy
                final LocalDate currentTargetDate = targetDate; // Finalna zmienna dla lambdy

                List<Availability> filtered = allAppointments.stream()
                        .filter(a -> !a.getIsBooked())
                        .filter(a -> doctorId == null || a.getDoctor().getId().equals(doctorId))
                        .filter(a -> specialization == null || a.getSpecialization().equalsIgnoreCase(specialization))
                        .filter(a -> isWithinDateRange(a, currentTargetDate, currentRange)) // Wywołanie funkcji pomocniczej
                        .sorted(Comparator.comparing(Availability::getAvailableTime))
                        .toList(); // Użycie .toList() zamiast Collectors.toList()

                results.addAll(filtered);
                if (!results.isEmpty()) break; // Przerwij, jeśli znaleziono wyniki w obecnym zakresie
            }

            // Jeśli brak wyników, znajdź pierwsze dostępne terminy
            if (results.isEmpty()) {
                results = allAppointments.stream()
                        .filter(a -> !a.getIsBooked())
                        .filter(a -> doctorId == null || a.getDoctor().getId().equals(doctorId))
                        .filter(a -> specialization == null || a.getSpecialization().equalsIgnoreCase(specialization))
                        .sorted(Comparator.comparing(Availability::getAvailableTime))
                        .toList();
            }

        } catch (Exception e) {
            logger.error("Błąd podczas wyszukiwania najbliższych wizyt: {}", e.getMessage());
        }

        return results;
    }

    /**
     * Funkcja pomocnicza sprawdzająca, czy termin jest w określonym zakresie dat.
     */
    private boolean isWithinDateRange(Availability availability, LocalDate targetDate, int range) {
        if (targetDate != null) {
            LocalDate appointmentDate = availability.getAvailableTime().toLocalDate();
            return appointmentDate.isEqual(targetDate) ||
                    (appointmentDate.isAfter(targetDate.minusDays(range)) && appointmentDate.isBefore(targetDate.plusDays(range)));
        }
        return true;
    }
}
