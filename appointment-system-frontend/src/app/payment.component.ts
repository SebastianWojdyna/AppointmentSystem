import { Component, OnInit } from '@angular/core';
import { AppointmentService } from './appointment.service';
import { DoctorService } from './doctor.service';
import { PaymentService } from './payment.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html'
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
    userId: null,
    paid: false
  };

  constructor(
    private appointmentService: AppointmentService,
    private doctorService: DoctorService,
    private paymentService: PaymentService
  ) {}

  ngOnInit() {
    this.loadAppointments();
    this.loadServices();
    this.loadDoctors();
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
    const loggedInUserId = localStorage.getItem('loggedInUserId');
    if (!loggedInUserId) {
      console.error('User ID is missing');
      return;
    }

    const selectedService = this.services.find(service => service.id === this.newAppointment.service?.id);
    if (!selectedService) {
      console.error('Wybrana usługa nie została znaleziona:', this.newAppointment.service?.id);
      return;
    }

    const selectedDoctor = this.doctors.find(doctor => doctor.id === this.newAppointment.doctor?.id);
    if (!selectedDoctor) {
      console.error('Wybrany lekarz nie został znaleziony:', this.newAppointment.doctor?.id);
      return;
    }

    this.newAppointment.service = selectedService;
    this.newAppointment.doctor = selectedDoctor;
    this.newAppointment.paid = true;
    this.newAppointment.userId = loggedInUserId;

    if (!this.newAppointment.patientName || !this.newAppointment.doctor || !this.newAppointment.appointmentTime || !this.newAppointment.service) {
      console.error('Wszystkie pola są wymagane');
      return;
    }

    console.log('Dane wizyty przed wysłaniem:', this.newAppointment);

    this.paymentService.createAppointment(this.newAppointment).subscribe((response: any) => {
      if (response && response.redirectUrl) {
        console.log('Wizyta utworzona i zapisana w bazie danych:', response);
        window.location.href = response.redirectUrl;
      } else {
        console.error('Nieprawidłowa odpowiedź:', response);
      }
    }, error => {
      console.error('Błąd podczas tworzenia wizyty', error);
    });
  }

  createAndPayOnSiteAppointment() {
    const loggedInUserId = localStorage.getItem('loggedInUserId');
    if (!loggedInUserId) {
      console.error('User ID is missing');
      return;
    }

    const selectedService = this.services.find(service => service.id === this.newAppointment.service?.id);
    if (!selectedService) {
      console.error('Wybrana usługa nie została znaleziona:', this.newAppointment.service?.id);
      return;
    }

    const selectedDoctor = this.doctors.find(doctor => doctor.id === this.newAppointment.doctor?.id);
    if (!selectedDoctor) {
      console.error('Wybrany lekarz nie został znaleziony:', this.newAppointment.doctor?.id);
      return;
    }

    this.newAppointment.service = selectedService;
    this.newAppointment.doctor = selectedDoctor;
    this.newAppointment.paid = false;
    this.newAppointment.userId = loggedInUserId;

    this.appointmentService.createAppointment(this.newAppointment).subscribe(response => {
      console.log('Wizyta utworzona pomyślnie z płatnością na miejscu:', response);
      this.loadAppointments(); // Odświeżenie listy wizyt
    }, error => {
      console.error('Błąd podczas tworzenia wizyty z płatnością na miejscu', error);
    });
  }
}
