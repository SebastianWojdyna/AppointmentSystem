import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-doctor-dashboard',
  templateUrl: './doctor-dashboard.component.html',
  styleUrls: ['./doctor-dashboard.component.css']
})
export class DoctorDashboardComponent implements OnInit {
  availableServices: any[] = []; // Typy wizyt dostępne w systemie
  availability: any[] = [];      // Lista dostępności lekarza
  selectedServiceId: number = 0;
  selectedDateTime: string = '';
  currentDoctorId: number | null = null;

  successMessage: string = '';
  errorMessage: string = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadDoctorData();
    this.loadAvailableServices();
    this.loadDoctorAvailability();
  }

  // Pobranie ID aktualnie zalogowanego lekarza
  loadDoctorData(): void {
    this.http.get<any>('http://localhost:8080/api/doctors/me').subscribe({
      next: (data) => {
        this.currentDoctorId = data.id;
      },
      error: (err) => {
        this.errorMessage = 'Nie udało się pobrać danych lekarza.';
        console.error(err);
      }
    });
  }

  // Pobranie usług medycznych z backendu
  loadAvailableServices(): void {
    this.http.get<any[]>('http://localhost:8080/api/services').subscribe({
      next: (data) => this.availableServices = data,
      error: (err) => this.errorMessage = 'Nie udało się pobrać usług.'
    });
  }

  // Pobranie dostępności lekarza
  loadDoctorAvailability(): void {
    this.http.get<any[]>('http://localhost:8080/api/appointments/doctor/me').subscribe({
      next: (data) => this.availability = data,
      error: (err) => this.errorMessage = 'Nie udało się pobrać dostępności.'
    });
  }

  // Dodanie nowej dostępności
  addAvailability(): void {
    if (!this.currentDoctorId) {
      this.errorMessage = 'Nie można dodać dostępności – brak danych lekarza.';
      return;
    }

    const requestBody = {
      appointmentTime: this.selectedDateTime,
      service: { id: this.selectedServiceId },
      doctor: { id: this.currentDoctorId }
    };

    this.http.post('http://localhost:8080/api/appointments/availability', requestBody).subscribe({
      next: () => {
        this.successMessage = 'Dostępność została dodana!';
        this.loadDoctorAvailability();
        this.errorMessage = '';
      },
      error: (err) => {
        this.errorMessage = 'Błąd przy dodawaniu dostępności.';
        console.error(err);
      }
    });
  }
}
