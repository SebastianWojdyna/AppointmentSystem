import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-doctor-dashboard',
  templateUrl: './doctor-dashboard.component.html',
  styleUrls: ['./doctor-dashboard.component.css']
})
export class DoctorDashboardComponent implements OnInit {
  availableServices: any[] = []; // Usługi dostępne w systemie
  availability: any[] = []; // Lista dostępności lekarza
  selectedServiceId: number = 0;
  selectedDateTime: string = '';
  price: number = 0; // Pole dla ceny
  currentDoctorId: number | null = null;

  successMessage: string = '';
  errorMessage: string = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadDoctorData();
    this.loadAvailableServices();
    this.loadDoctorAvailability();
  }

  // Pobranie danych aktualnie zalogowanego lekarza
  loadDoctorData(): void {
    this.http.get<any>('http://localhost:8080/api/doctors/me').subscribe({
      next: (data) => this.currentDoctorId = data.id,
      error: (err) => {
        this.errorMessage = 'Nie udało się pobrać danych lekarza.';
        console.error(err);
      }
    });
  }

  // Pobranie listy dostępnych usług
  loadAvailableServices(): void {
    this.http.get<any[]>('http://localhost:8080/api/services').subscribe({
      next: (data) => this.availableServices = data,
      error: (err) => {
        this.errorMessage = 'Nie udało się pobrać usług.';
        console.error(err);
      }
    });
  }

  // Pobranie dostępności lekarza
  loadDoctorAvailability(): void {
    if (this.currentDoctorId) { // Sprawdzenie, czy currentDoctorId jest ustawiony
      this.http.get<any[]>(`http://localhost:8080/api/availability/doctor/${this.currentDoctorId}`).subscribe({
        next: (data) => this.availability = data,
        error: (err) => {
          this.errorMessage = 'Nie udało się pobrać dostępności.';
          console.error(err);
        }
      });
    } else {
      console.warn('Doctor ID is not set. Availability cannot be loaded.');
    }
  }


  // Dodanie nowej dostępności
  addAvailability(): void {
    if (!this.selectedServiceId || !this.selectedDateTime || this.price <= 0) {
      this.errorMessage = 'Wszystkie pola są wymagane.';
      return;
    }

    const requestBody = {
      serviceId: this.selectedServiceId,
      availableTimes: [this.selectedDateTime],
      price: this.price
    };

    this.http.post('http://localhost:8080/api/availability/add', requestBody).subscribe({
      next: () => {
        this.successMessage = 'Dostępność została dodana!';
        this.clearForm();
        this.loadDoctorAvailability();
      },
      error: (err) => {
        this.errorMessage = 'Błąd przy dodawaniu dostępności.';
        console.error(err);
      }
    });
  }

  clearForm(): void {
    this.selectedServiceId = 0;
    this.selectedDateTime = '';
    this.price = 0;
    this.errorMessage = '';
  }
}
