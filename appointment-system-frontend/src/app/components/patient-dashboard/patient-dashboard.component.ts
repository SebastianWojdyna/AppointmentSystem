import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
    selector: 'app-patient-dashboard',
    templateUrl: './patient-dashboard.component.html',
    styleUrls: ['./patient-dashboard.component.css'],
    standalone: false
})
export class PatientDashboardComponent implements OnInit {
  availableAppointments: any[] = [];
  filteredAppointments: any[] = [];
  recommendedAppointments: any[] = [];
  specializations: string[] = [];
  doctors: string[] = [];
  filterDate: string = '';
  filterSpecialization: string = 'Wybierz specjalizację';
  filterDoctor: string = 'Wybierz lekarza';
  successMessage: string = '';
  errorMessage: string = '';
  selectedAppointmentId: number | null = null;

  patientDetails = {
    firstName: '',
    lastName: '',
    pesel: '',
    gender: 'Mężczyzna',
    birthDate: '',
    symptoms: ''
  };

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadAvailableAppointments();
    this.filteredAppointments = [];  // Początkowo lista pusta
  }

  openPatientForm(availabilityId: number): void {
    this.selectedAppointmentId = availabilityId;
    this.resetForm();
    $('#patientDetailsModal').modal('show');
  }

  confirmBooking(): void {
    if (this.selectedAppointmentId) {
      const bookingRequest = {
        availabilityId: this.selectedAppointmentId,
        ...this.patientDetails
      };

      this.http.post(
        'https://appointment-system-backend.azurewebsites.net/api/availability/book-with-details',
        bookingRequest
      ).subscribe({
        next: () => {
          this.successMessage = 'Wizyta została zarezerwowana!';
          $('#patientDetailsModal').modal('hide');
          this.loadAvailableAppointments();
        },
        error: (err) => {
          this.errorMessage = 'Nie udało się zarezerwować wizyty.';
          console.error(err);
        }
      });
    }
  }

  resetForm(): void {
    this.patientDetails = {
      firstName: '',
      lastName: '',
      pesel: '',
      gender: 'Mężczyzna',
      birthDate: '',
      symptoms: ''
    };
  }

  loadAvailableAppointments(): void {
    this.http.get<any[]>('https://appointment-system-backend.azurewebsites.net/api/availability').subscribe({
      next: (data) => {
        this.availableAppointments = data
          .filter(appt => !appt.isBooked)
          .map(appt => ({
            ...appt,
            availableTime: this.parseDate(appt.availableTime)
          }));
        this.extractFilters();
      },
      error: (err) => {
        this.errorMessage = 'Nie udało się pobrać dostępnych wizyt.';
        console.error('Failed to load appointments:', err);
      }
    });
  }

  extractFilters(): void {
    const specs = new Set(this.availableAppointments.map(appt => appt.specialization));
    this.specializations = Array.from(specs);

    const doctorNames = new Set(this.availableAppointments.map(appt => appt.doctor.name));
    this.doctors = Array.from(doctorNames);
  }

  applyFilters(): void {
    console.log('Wywołano applyFilters');
    this.filteredAppointments = this.availableAppointments.filter(appt => {
      const matchesDate = this.filterDate ? appt.availableTime.startsWith(this.filterDate) : true;
      const matchesSpec = this.filterSpecialization !== 'Wybierz specjalizację' ? appt.specialization === this.filterSpecialization : true;
      const matchesDoctor = this.filterDoctor !== 'Wybierz lekarza' ? appt.doctor.name === this.filterDoctor : true;
      return matchesDate && matchesSpec && matchesDoctor;
    });

    if (this.filteredAppointments.length === 0) {
      console.log('Brak wizyt, uruchamiam rekomendacje...');
      this.getRecommendations();
    }
  }

  resetFilters(): void {
    this.filterDate = '';
    this.filterSpecialization = 'Wybierz specjalizację';
    this.filterDoctor = 'Wybierz lekarza';
    this.filteredAppointments = [];  // Pusta lista po resecie
  }


  bookAppointment(availabilityId: number): void {
    this.http.post(`https://appointment-system-backend.azurewebsites.net/api/availability/book/${availabilityId}`, null).subscribe({
      next: () => {
        this.successMessage = 'Wizyta została zarezerwowana!';
        this.loadAvailableAppointments();
      },
      error: (err) => {
        this.errorMessage = 'Nie udało się zarezerwować wizyty.';
        console.error(err);
      }
    });
  }

  parseDate(rawDate: any): string | null {
    try {
      if (!rawDate) return null;
      const [year, month, day, hours, minutes] = rawDate;
      return new Date(year, month - 1, day, hours, minutes).toISOString();
    } catch (error) {
      console.error('Error parsing date:', rawDate, error);
      return null;
    }
  }

  getRecommendations(): void {
    console.log('Wywołano getRecommendations');

    const params: any = {
      specialization: this.filterSpecialization !== 'Wybierz specjalizację' ? this.filterSpecialization : '',
      doctorId: this.filterDoctor !== 'Wybierz lekarza' ? this.getDoctorId(this.filterDoctor) : ''
    };

    if (this.filterDate) {
      params.date = this.filterDate; // Dodajemy `date` tylko jeśli nie jest puste
    }

    this.http.get<any[]>('https://appointment-system-backend.azurewebsites.net/api/availability/recommendations', { params })
      .subscribe({
        next: (recommendations) => {
          console.log('Rekomendacje z API:', recommendations);

          // Grupowanie rekomendacji według opisu (description)
          const groupedRecommendations: { [key: string]: any[] } = {};
          recommendations.forEach(appt => {
            const description = appt.description || 'Inne';
            if (!groupedRecommendations[description]) {
              groupedRecommendations[description] = [];
            }
            groupedRecommendations[description].push({
              ...appt,
              availableTime: this.parseDate(appt.availableTime)
            });
          });

          // Konwersja grupowanych danych do listy, którą można łatwo iterować w Angularze
          this.recommendedAppointments = Object.entries(groupedRecommendations).map(([key, value]) => ({
            description: key,
            appointments: value
          }));

          console.log('Zgrupowane rekomendacje:', this.recommendedAppointments);
          $('#recommendationsModal').modal('show');
        },
        error: (err) => {
          console.error('Błąd pobierania rekomendacji:', err);
        }
      });
  }

  getDoctorId(doctorName: string): string {
    const doctor = this.availableAppointments.find(appt => appt.doctor.name === doctorName);
    return doctor ? doctor.doctor.id : '';
  }
}
