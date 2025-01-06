import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-patient-dashboard',
  templateUrl: './patient-dashboard.component.html',
  styleUrls: ['./patient-dashboard.component.css']
})
export class PatientDashboardComponent implements OnInit {
  availableAppointments: any[] = [];
  filteredAppointments: any[] = [];
  specializations: string[] = [];
  doctors: string[] = [];
  filterDate: string = '';
  filterSpecialization: string = 'Wybierz specjalizację';
  filterDoctor: string = 'Wybierz lekarza';
  successMessage: string = '';
  errorMessage: string = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadAvailableAppointments();
    this.filteredAppointments = [];  // Początkowo lista pusta
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
    this.filteredAppointments = this.availableAppointments.filter(appt => {
      const matchesDate = this.filterDate ? appt.availableTime.startsWith(this.filterDate) : true;
      const matchesSpec = this.filterSpecialization !== 'Wybierz specjalizację' ? appt.specialization === this.filterSpecialization : true;
      const matchesDoctor = this.filterDoctor !== 'Wybierz lekarza' ? appt.doctor.name === this.filterDoctor : true;
      return matchesDate && matchesSpec && matchesDoctor;
    });

    // Jeśli żaden filtr nie jest wybrany, lista pozostaje pusta
    if (!this.filterDate && this.filterSpecialization === 'Wybierz specjalizację' && this.filterDoctor === 'Wybierz lekarza') {
      this.filteredAppointments = [];
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
}
