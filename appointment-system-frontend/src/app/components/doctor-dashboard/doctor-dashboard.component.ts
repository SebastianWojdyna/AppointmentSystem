import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-doctor-dashboard',
  templateUrl: './doctor-dashboard.component.html',
  styleUrls: ['./doctor-dashboard.component.css']
})
export class DoctorDashboardComponent implements OnInit {
  availableServices: any[] = [];
  availability: any[] = [];
  selectedServiceId: number = 0;
  selectedDateTime: string = '';
  price: number = 0;
  currentDoctorId: number | null = null;

  newServiceName: string = '';
  newServicePrice: number | null = null;

  successMessage: string = '';
  errorMessage: string = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadDoctorData();
    this.loadAvailableServices();
  }

  loadDoctorData(): void {
    this.http.get<any>('http://localhost:8080/api/doctors/me').subscribe({
      next: (data) => {
        this.currentDoctorId = data.id;
        this.loadDoctorAvailability();
      },
      error: (err) => {
        this.errorMessage = 'Nie udało się pobrać danych lekarza.';
        console.error(err);
      }
    });
  }

  loadAvailableServices(): void {
    this.http.get<any[]>('http://localhost:8080/api/services').subscribe({
      next: (data) => (this.availableServices = data),
      error: (err) => {
        this.errorMessage = 'Nie udało się pobrać usług.';
        console.error(err);
      }
    });
  }

  loadDoctorAvailability(): void {
    if (this.currentDoctorId) {
      this.http
        .get<any[]>(`http://localhost:8080/api/availability/doctor/${this.currentDoctorId}`)
        .subscribe({
          next: (data) => {
            this.availability = data.map(avail => ({
              id: avail.id,
              service: avail.service || { name: 'Nieznana usługa' },
              availableTime: this.parseDate(avail.availableTime),
              price: avail.price || 0,
              specialization: avail.specialization || 'Brak specjalizacji',
              isBooked: avail.isBooked || false // Flaga rezerwacji
            }));
          },
          error: (err) => {
            this.errorMessage = 'Nie udało się pobrać dostępności.';
            console.error(err);
          }
        });
    }
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

    this.http.post('http://localhost:8080/api/availability/add', requestBody, { responseType: 'text' }).subscribe({
      next: (response) => {
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

  deleteAvailability(availabilityId: number): void {
    if (!availabilityId) return;

    this.http.delete(`http://localhost:8080/api/availability/delete/${availabilityId}`).subscribe({
      next: () => {
        this.successMessage = 'Dostępność została usunięta!';
        this.loadDoctorAvailability(); // Odśwież listę dostępności
      },
      error: (err) => {
        this.errorMessage = 'Błąd przy usuwaniu dostępności.';
        console.error(err);
      }
    });
  }

  addNewService(): void {
    if (!this.newServiceName || this.newServicePrice === null || this.newServicePrice <= 0) {
      this.errorMessage = 'Wprowadź poprawną nazwę i cenę usługi.';
      return;
    }

    const newService = { name: this.newServiceName.trim(), price: this.newServicePrice };

    this.http.post<any>('http://localhost:8080/api/services/add', newService, {
      headers: { 'Content-Type': 'application/json' }
    }).subscribe({
      next: (data) => {
        this.successMessage = 'Nowy typ usługi został dodany!';
        this.loadAvailableServices();
        this.clearNewServiceForm();
      },
      error: (err) => {
        this.errorMessage = err.error || 'Nie udało się dodać nowego typu usługi.';
        console.error(err);
      }
    });
  }

  clearForm(): void {
    this.selectedServiceId = 0;
    this.selectedDateTime = '';
    this.price = 0;
    this.errorMessage = '';
    this.successMessage = '';
  }

  clearNewServiceForm(): void {
    this.newServiceName = '';
    this.newServicePrice = null;
  }
}
