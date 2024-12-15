import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AppointmentService } from './appointment.service';
import { PaymentService } from './payment.service';
import { DoctorService } from './doctor.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'System Rezerwacji Wizyt';
  appointments: any[] = [];
  services: any[] = [];
  doctors: any[] = [];
  newAppointment: any = {
    patientName: '',
    doctor: null,
    appointmentTime: '',
    service: null,
    userId: null,
    paid: false
  };

  constructor(
    private appointmentService: AppointmentService,
    private paymentService: PaymentService,
    private doctorService: DoctorService,
    private router: Router
  ) {}

  ngOnInit(): void {
    console.log('Logged in user:', localStorage.getItem('token')); // Debugowanie tokena JWT
    this.loadAppointments();
    this.loadServices();
    this.loadDoctors();
  }

  // Getter do sprawdzania, czy użytkownik jest zalogowany (sprawdza obecność tokena JWT w localStorage)
  public get isLoggedIn(): boolean {
    const token = !!localStorage.getItem('token');
    console.log('Getter isLoggedIn called, value:', token); // Debugowanie stanu zalogowania
    return token;
  }

  loadAppointments(): void {
    this.appointmentService.getAppointments().subscribe(data => {
      this.appointments = data.sort(
        (a, b) =>
          new Date(a.appointmentTime).getTime() - new Date(b.appointmentTime).getTime()
      );
    });
  }

  loadServices(): void {
    this.appointmentService.getServices().subscribe(data => {
      this.services = data;
    });
  }

  loadDoctors(): void {
    this.doctorService.getDoctors().subscribe(data => {
      this.doctors = data;
    });
  }

  createAndPayAppointment(): void {
    const selectedService = this.services.find(
      service => service.id === this.newAppointment.service?.id
    );
    if (!selectedService) {
      console.error('Wybrana usługa nie została znaleziona:', this.newAppointment.service?.id);
      return;
    }

    this.newAppointment.service = selectedService;
    this.newAppointment.paid = true;
    this.newAppointment.userId = localStorage.getItem('loggedInUserId');

    if (
      !this.newAppointment.patientName ||
      !this.newAppointment.doctor ||
      !this.newAppointment.appointmentTime ||
      !this.newAppointment.service
    ) {
      console.error('Wszystkie pola są wymagane');
      return;
    }

    this.paymentService.createAppointment(this.newAppointment).subscribe(
      (response: any) => {
        if (response && response.orderId) {
          this.paymentService.processPayment(response.orderId).subscribe(
            (paymentResponse: any) => {
              window.location.href = paymentResponse.redirectUrl;
            },
            error => {
              console.error('Błąd podczas przetwarzania płatności', error);
            }
          );
        } else {
          console.error('Nieprawidłowa odpowiedź:', response);
        }
      },
      error => {
        console.error('Błąd podczas tworzenia wizyty', error);
      }
    );
  }

  createAndPayOnSiteAppointment(): void {
    const selectedService = this.services.find(
      service => service.id === this.newAppointment.service?.id
    );
    if (!selectedService) {
      console.error('Wybrana usługa nie została znaleziona:', this.newAppointment.service?.id);
      return;
    }

    this.newAppointment.service = selectedService;
    this.newAppointment.paid = false;
    this.newAppointment.userId = localStorage.getItem('loggedInUserId');

    this.appointmentService.createAppointment(this.newAppointment).subscribe(
      response => {
        console.log('Wizyta utworzona pomyślnie z płatnością na miejscu:', response);
        this.loadAppointments(); // Odśwież listę wizyt
      },
      error => {
        console.error('Błąd podczas tworzenia wizyty z płatnością na miejscu', error);
      }
    );
  }

  logout(): void {
    localStorage.removeItem('token'); // Usuń token JWT
    localStorage.removeItem('loggedInUserId'); // Usuń ID użytkownika
    this.router.navigate(['/login']); // Przekierowanie na stronę logowania
    console.log('User logged out, isLoggedIn:', this.isLoggedIn); // Debugowanie wylogowania
  }

  navigateToRegister(): void {
    this.router.navigate(['/register']);
  }

  navigateToLogin(): void {
    this.router.navigate(['/login']);
  }
}
