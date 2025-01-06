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
  reservedAppointments: any[] = [];
  specializations: string[] = [];
  doctors: string[] = [];
  filterDate: string = '';
  filterSpecialization: string = '';
  filterDoctor: string = '';
  successMessage: string = '';
  errorMessage: string = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadAvailableAppointments();
    this.loadReservedAppointments();
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
        this.filteredAppointments = [...this.availableAppointments];
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
      const matchesDate = this.filterDate
        ? appt.availableTime.slice(0, 10) === this.filterDate
        : true;
      const matchesSpec = this.filterSpecialization
        ? appt.specialization === this.filterSpecialization
        : true;
      const matchesDoctor = this.filterDoctor
        ? appt.doctor.name === this.filterDoctor
        : true;
      return matchesDate && matchesSpec && matchesDoctor;
    });
  }

  resetFilters(): void {
    this.filterDate = '';
    this.filterSpecialization = '';
    this.filterDoctor = '';
    this.filteredAppointments = [...this.availableAppointments];
  }

  loadReservedAppointments(): void {
    this.http.get<any[]>('https://appointment-system-backend.azurewebsites.net/api/availability/reserved').subscribe({
      next: (data) => {
        this.reservedAppointments = data.map(appt => ({
          ...appt,
          availableTime: this.parseDate(appt.availableTime)
        }));
      },
      error: (err) => {
        this.errorMessage = 'Nie udało się pobrać zarezerwowanych wizyt.';
        console.error('Failed to load reserved appointments:', err);
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

cancelAppointment(availabilityId: number): void {
    this.http.delete(`https://appointment-system-backend.azurewebsites.net/api/availability/cancel/${availabilityId}`).subscribe({
      next: () => {
        this.successMessage = "Rezerwacja została anulowana!";
        this.loadAvailableAppointments();
        this.loadReservedAppointments();
      },
      error: (err) => {
        this.errorMessage = "Nie udało się anulować rezerwacji!";
        console.error(err);
      }
    });
  }
}
