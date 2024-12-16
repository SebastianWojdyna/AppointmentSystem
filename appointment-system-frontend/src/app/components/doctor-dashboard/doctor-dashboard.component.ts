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
  }

  // Pobranie danych aktualnie zalogowanego lekarza
  loadDoctorData(): void {
    this.http.get<any>('http://localhost:8080/api/doctors/me').subscribe({
      next: (data) => {
        this.currentDoctorId = data.id;
        this.loadDoctorAvailability(); // Ładowanie dostępności po uzyskaniu ID
      },
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
    if (this.currentDoctorId) {
      this.http.get<any[]>(`http://localhost:8080/api/availability/doctor/${this.currentDoctorId}`)
        .subscribe({
          next: (data) => {
            this.availability = data.map(avail => ({
              id: avail.id,
              service: avail.service || { name: 'Nieznana usługa' },
              availableTime: this.parseDate(avail.availableTime), // Poprawione parsowanie daty
              price: avail.price || 0,
              specialization: avail.specialization || 'Brak specjalizacji'
            }));
          },
          error: (err) => {
            this.errorMessage = 'Nie udało się pobrać dostępności.';
            console.error(err);
          }
        });
    }
  }


  // Funkcja do parsowania daty z formatu "2024,12,7,2,55" do "2024-12-07T02:55:00"
  parseDate(rawDate: any): string | null {
    try {
      if (!rawDate) return null;

      let dateParts: number[];

      // Sprawdzamy, czy rawDate jest tablicą
      if (Array.isArray(rawDate)) {
        dateParts = rawDate.map(part => parseInt(part, 10));
      }
      // Jeśli to ciąg znaków, dzielimy po przecinkach
      else if (typeof rawDate === 'string') {
        dateParts = rawDate.split(',').map(part => parseInt(part, 10));
      }
      // Jeśli nie spełnia warunków, zwracamy null
      else {
        console.error('Unsupported date format:', rawDate);
        return null;
      }

      // Parsowanie daty z części
      if (dateParts.length >= 5) {
        const [year, month, day, hours, minutes] = dateParts;
        const formattedDate = new Date(year, month - 1, day, hours, minutes);
        return formattedDate.toISOString(); // Format ISO "YYYY-MM-DDTHH:mm:ss"
      }
    } catch (error) {
      console.error('Error parsing date:', rawDate, error);
    }
    return null; // W przypadku błędu
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

    this.http.post('http://localhost:8080/api/availability/add', requestBody, { responseType: 'text' }).subscribe({
      next: (response) => {
        this.successMessage = response || 'Dostępność została dodana!';
        this.clearForm();
        this.loadDoctorAvailability();
      },
      error: (err) => {
        this.errorMessage = 'Błąd przy dodawaniu dostępności.';
        console.error('Błąd dodawania dostępności:', err);
      }
    });
  }

  // Czyszczenie formularza
  clearForm(): void {
    this.selectedServiceId = 0;
    this.selectedDateTime = '';
    this.price = 0;
    this.errorMessage = '';
    this.successMessage = '';
  }
}
