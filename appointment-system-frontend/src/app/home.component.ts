import { Component, OnInit } from '@angular/core';
import { AppointmentService } from './appointment.service';
import { DoctorService } from './doctor.service';
import { PaymentService } from './payment.service';
import 'jquery';
import 'bootstrap';

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.css'],
    standalone: false
})
export class HomeComponent implements OnInit {
  appointments: any[] = [];
  services: any[] = [];
  doctors: any[] = [];
  newAppointment: any = {
    patientName: '',
    doctor: null,
    appointmentTime: '',
    service: null,
    userId: ''  // Dodanie userId do nowej wizyty
  };
  appointmentToCancel: any = null;

  constructor(
    private appointmentService: AppointmentService,
    private doctorService: DoctorService,
    private paymentService: PaymentService
  ) {}

  ngOnInit() {
    this.loadAppointments();
    this.loadServices();
    this.loadDoctors();
    this.newAppointment.userId = localStorage.getItem('loggedInUserId');  // Pobranie userId z localStorage
    console.log('Logged in user:', localStorage.getItem('loggedInUserId'));  // Debugowanie userId
  }

  loadAppointments() {
    this.appointmentService.getAppointments().subscribe(data => {
      this.appointments = data.sort((a, b) => new Date(a.appointmentTime).getTime() - new Date(b.appointmentTime).getTime());
    });
  }

  loadServices() {
    this.appointmentService.getServices().subscribe(data => {
      this.services = data;
    });
  }

  loadDoctors() {
    this.doctorService.getDoctors().subscribe(data => {
      this.doctors = data;
    });
  }

  createAndPayAppointment() {
    const userId = parseInt(localStorage.getItem('loggedInUserId')!, 10);
    if (!userId) {
      console.error('User ID is missing');
      return;
    }

    const selectedService = this.services.find(service => service.id === this.newAppointment.service?.id);
    if (!selectedService) {
      console.error('Wybrana usługa nie została znaleziona:', this.newAppointment.service?.id);
      return;
    }

    this.newAppointment.service = selectedService;
    this.newAppointment.paid = true;
    this.newAppointment.userId = userId; // Konwersja userId na liczbę
    this.newAppointment.doctorId = this.newAppointment.doctor.id; // Dodanie doctorId
    this.newAppointment.serviceId = this.newAppointment.service.id; // Dodanie serviceId

    console.log('Dane wizyty przed wysłaniem:', this.newAppointment);

    // Zapisywanie danych w lokalnej pamięci przed wysłaniem
    localStorage.setItem('appointmentRequest', JSON.stringify(this.newAppointment));

    this.paymentService.createAppointment(this.newAppointment).subscribe((response: any) => {
      if (response && response.redirectUrl) {
        console.log('Wizyta utworzona i zapisana w bazie danych:', response);
        localStorage.setItem('appointmentResponse', JSON.stringify(response));
        window.location.href = response.redirectUrl; // Przekierowanie użytkownika do płatności
      } else {
        console.error('Nieprawidłowa odpowiedź:', response);
      }
    }, error => {
      console.error('Błąd podczas tworzenia wizyty', error);
    });
  }

  createAndPayOnSiteAppointment() {
    const selectedService = this.services.find(service => service.id === this.newAppointment.service?.id);
    if (!selectedService) {
      console.error('Wybrana usługa nie została znaleziona:', this.newAppointment.service?.id);
      return;
    }

    this.newAppointment.service = selectedService;
    this.newAppointment.paid = false; // Ustawienie paid na false

    this.appointmentService.createAppointment(this.newAppointment).subscribe(response => {
      console.log('Wizyta utworzona pomyślnie z płatnością na miejscu:', response);
      this.loadAppointments(); // Odśwież listę wizyt
    }, error => {
      console.error('Błąd podczas tworzenia wizyty z płatnością na miejscu', error);
    });
  }

  cancelAppointment(appointment: any) {
    if (appointment.paid) {
      this.appointmentToCancel = appointment;
      $('#confirmCancelModal').modal('show');  // Wyświetlenie modal potwierdzenia
    } else {
      this.appointmentService.cancelAppointment(appointment.id).subscribe(() => {
        alert('Twoja wizyta została odwołana pomyślnie');
        this.loadAppointments(); // Odśwież listę wizyt
      }, error => {
        console.error('Błąd podczas odwoływania wizyty', error);
      });
    }
  }

  confirmCancel() {
    if (this.appointmentToCancel) {
      this.appointmentService.cancelAppointment(this.appointmentToCancel.id).subscribe(() => {
        alert('Twoja wizyta została odwołana pomyślnie');
        this.loadAppointments(); // Odśwież listę wizyt
        $('#confirmCancelModal').modal('hide');
        this.appointmentToCancel = null;
      }, error => {
        console.error('Błąd podczas odwoływania wizyty', error);
      });
    }
  }
}
