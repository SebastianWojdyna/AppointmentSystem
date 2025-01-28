import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'app-reserved-appointments',
    templateUrl: './reserved-appointments.component.html',
    styleUrls: ['./reserved-appointments.component.css'],
    standalone: false
})
export class ReservedAppointmentsComponent implements OnInit {
  reservedAppointments: any[] = [];
  selectedPatientDetails: any = null;
  successMessage: string = '';
  errorMessage: string = '';

  constructor(private http: HttpClient, private modalService: NgbModal) {}

  ngOnInit(): void {
    this.loadReservedAppointments();
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

  openPatientDetailsModal(content: any, patientDetails: any): void {
    this.selectedPatientDetails = { ...patientDetails };
    this.modalService.open(content, { ariaLabelledBy: 'modal-basic-title' });
  }

  savePatientDetails(): void {
    if (this.selectedPatientDetails) {
      this.http.put<{ message: string }>( // <-- Oczekujemy odpowiedzi zawierającej `message`
        `https://appointment-system-backend.azurewebsites.net/api/availability/patient-details/${this.selectedPatientDetails.availabilityId}`,
        this.selectedPatientDetails
      ).subscribe({
        next: (response) => {
          this.showSuccessMessage(response.message); // <-- Pobieramy komunikat od backendu
          this.loadReservedAppointments();
          this.modalService.dismissAll();
        },
        error: (err) => {
          this.showErrorMessage('Nie udało się zaktualizować danych pacjenta.');
          console.error('Failed to update patient details:', err);
        }
      });
    }
  }

  // Anuluje wizytę
  cancelAppointment(availabilityId: number): void {
    this.http.delete<{ message: string }>( // <-- Oczekujemy odpowiedzi zawierającej `message`
      `https://appointment-system-backend.azurewebsites.net/api/availability/cancel/${availabilityId}`
    ).subscribe({
      next: (response) => {
        this.showSuccessMessage(response.message); // <-- Pobieramy komunikat od backendu
        this.loadReservedAppointments();
      },
      error: (err) => {
        this.showErrorMessage('Nie udało się anulować rezerwacji.');
        console.error('Failed to cancel appointment:', err);
      }
    });
  }

  // Parsowanie daty, aby była odpowiednio wyświetlana
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

  private showSuccessMessage(message: string): void {
    this.successMessage = message;
    setTimeout(() => { this.successMessage = ''; }, 4000); // Komunikat znika po 4 sekundach
  }

  private showErrorMessage(message: string): void {
    this.errorMessage = message;
    setTimeout(() => { this.errorMessage = ''; }, 4000); // Komunikat znika po 4 sekundach
  }


}
