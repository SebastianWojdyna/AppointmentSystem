import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-patient-dashboard',
  templateUrl: './patient-dashboard.component.html',
  styleUrls: ['./patient-dashboard.component.css']
})
export class PatientDashboardComponent implements OnInit {
  availableAppointments: any[] = [];
  reservedAppointments: any[] = []; // Nowa lista zarezerwowanych wizyt
  successMessage: string = '';
  errorMessage: string = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadAvailableAppointments();
    this.loadReservedAppointments();
  }

  loadAvailableAppointments(): void {
    this.http.get<any[]>('http://localhost:8080/api/availability').subscribe({
      next: (data) => {
        this.availableAppointments = data
          .filter(appt => !appt.isBooked)
          .map(appt => ({
            ...appt,
            availableTime: this.parseDate(appt.availableTime)
          }));
      },
      error: (err) => {
        this.errorMessage = 'Nie udało się pobrać dostępnych wizyt.';
        console.error('Failed to load appointments:', err);
      }
    });
  }

  loadReservedAppointments(): void {
    this.http.get<any[]>('http://localhost:8080/api/availability/reserved').subscribe({
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

  bookAppointment(availabilityId: number): void {
    this.http.post(`http://localhost:8080/api/availability/book/${availabilityId}`, null).subscribe({
      next: () => {
        this.successMessage = 'Wizyta została zarezerwowana!';
        this.loadAvailableAppointments();
        this.loadReservedAppointments(); // Odśwież również zarezerwowane wizyty
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

  cancelAppointment(availabilityId: number): void{
    this.http.delete(`http://localhost:8080/api/availability/cancel/${availabilityId}`).subscribe({
      next: () => {
        this.successMessage = "Rezerwacja zostala anulowana!";
        this.loadAvailableAppointments();
      },
      error: (err) => {
        this.errorMessage = "Nie udalo sie anulowac rezerwacji!";
        console.error(err);
      }
    });
  }
}
