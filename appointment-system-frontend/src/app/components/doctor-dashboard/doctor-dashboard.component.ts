import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
    selector: 'app-doctor-dashboard',
    templateUrl: './doctor-dashboard.component.html',
    styleUrls: ['./doctor-dashboard.component.css'],
    standalone: false
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

  editingAvailability: any = null;  // Dostępność do edycji
  editingService: any = null;       // Usługa do edycji

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadDoctorData();
    this.loadAvailableServices();
  }

  loadDoctorData(): void {
    this.http.get<any>('https://appointment-system-backend.azurewebsites.net/api/doctors/me').subscribe({
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
    this.http.get<any[]>('https://appointment-system-backend.azurewebsites.net/api/services').subscribe({
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
        .get<any[]>(`https://appointment-system-backend.azurewebsites.net/api/availability/doctor/${this.currentDoctorId}`)
        .subscribe({
          next: (data) => {
            this.availability = data.map(avail => ({
              id: avail.id,
              service: avail.service || { name: 'Nieznana usługa' },
              availableTime: this.parseDate(avail.availableTime),
              price: avail.price || 0,
              specialization: avail.specialization || 'Brak specjalizacji',
              isBooked: avail.isBooked || false
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
    if (!this.selectedServiceId || !this.selectedDateTime) {
      this.errorMessage = 'Wybierz usługę i datę.';
      return;
    }

    const requestBody = {
      serviceId: this.selectedServiceId,
      availableTimes: [this.selectedDateTime]
    };

    this.http.post('https://appointment-system-backend.azurewebsites.net/api/availability/add', requestBody, { responseType: 'text' }).subscribe({
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

  deleteAvailability(availabilityId: number): void {
    if (!availabilityId) return;

    const appointment = this.availability.find(a => a.id === availabilityId);
    if (appointment?.isBooked) {
      const confirmed = confirm('Ta wizyta jest zarezerwowana. Czy na pewno chcesz ją usunąć?');
      if (!confirmed) {
        return;
      }
    }

    this.http.delete(`https://appointment-system-backend.azurewebsites.net/api/availability/delete/${availabilityId}`).subscribe({
      next: () => {
        this.successMessage = 'Dostępność została usunięta!';
        this.loadDoctorAvailability();
      },
      error: (err) => {
        this.errorMessage = 'Błąd przy usuwaniu dostępności.';
        console.error(err);
      }
    });
  }

  editAvailability(avail: any): void {
    this.editingAvailability = { ...avail };
  }

  updateAvailability(): void {
    const updatedAvailability = {
      serviceId: this.editingAvailability.service.id,
      availableTime: this.editingAvailability.availableTime,
    };

    this.http.put(`https://appointment-system-backend.azurewebsites.net/api/availability/${this.editingAvailability.id}`, updatedAvailability).subscribe({
      next: () => {
        this.successMessage = 'Dostępność została zaktualizowana.';
        this.editingAvailability = null;
        this.loadDoctorAvailability();
      },
      error: (err) => {
        this.errorMessage = 'Nie udało się zaktualizować dostępności.';
      }
    });
  }

  editService(service: any): void {
    this.editingService = { ...service };
  }

  updateService(): void {
    const updatedService = {
      name: this.editingService.name,
      price: this.editingService.price
    };

    this.http.put(`https://appointment-system-backend.azurewebsites.net/api/services/${this.editingService.id}`, updatedService).subscribe({
      next: () => {
        this.successMessage = 'Usługa została zaktualizowana.';
        this.editingService = null;
        this.loadAvailableServices();
        this.resetMessages();
      },
      error: (err) => {
        this.errorMessage = 'Nie udało się zaktualizować usługi.';
        this.resetMessages();
      }
    });
  }

  resetMessages(): void {
    setTimeout(() => {
      this.successMessage = '';
      this.errorMessage = '';
    }, 3000);  // Komunikat znika po 3 sekundach
  }

  deleteService(serviceId: number): void {
    const confirmed = confirm('Czy na pewno chcesz usunąć tę usługę?');
    if (!confirmed) return;

    this.http.delete(`https://appointment-system-backend.azurewebsites.net/api/services/${serviceId}`).subscribe({
      next: () => {
        this.successMessage = 'Usługa została usunięta!';
        this.loadAvailableServices();
      },
      error: (err) => {
        if (err.error && err.error.error) {
          this.errorMessage = err.error.error;
        } else {
          this.errorMessage = 'Nie udało się usunąć usługi ponieważ jest ona użyta w twoich dostępnościach..';
        }
      }
    });
  }

  addNewService(): void {
    if (!this.newServiceName || this.newServicePrice === null || this.newServicePrice <= 0) {
      this.errorMessage = 'Wprowadź poprawną nazwę i cenę usługi.';
      return;
    }

    const newService = { name: this.newServiceName.trim(), price: this.newServicePrice };

    this.http.post<any>('https://appointment-system-backend.azurewebsites.net/api/services/add', newService, {
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
    this.editingAvailability = null;
    this.errorMessage = '';
    this.successMessage = '';
  }

  clearNewServiceForm(): void {
    this.newServiceName = '';
    this.newServicePrice = null;
    this.editingService = null;
  }

  updatePrice(): void {
    const selectedService = this.availableServices.find(service => service.id === this.selectedServiceId);
    if (selectedService) {
      this.price = selectedService.price;
    }
  }
}
