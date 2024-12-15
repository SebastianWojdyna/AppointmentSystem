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
  role: string | null = null; // Zmienna do przechowywania roli użytkownika

  constructor(
    private router: Router
  ) {}

  ngOnInit(): void {
    this.checkUserRole();
  }

  // Sprawdzenie roli użytkownika
  checkUserRole(): void {
    this.role = localStorage.getItem('role'); // Pobierz rolę z localStorage
    console.log('User role:', this.role);
  }

  // Getter sprawdzający, czy użytkownik jest zalogowany
  public get isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  // Getter sprawdzający, czy użytkownik jest adminem
  public get isAdmin(): boolean {
    return this.role === 'ADMIN';
  }

  // Getter sprawdzający, czy użytkownik jest pacjentem
  public get isPatient(): boolean {
    return this.role === 'PATIENT';
  }

  logout(): void {
    localStorage.removeItem('token'); // Usuń token JWT
    localStorage.removeItem('role');  // Usuń rolę
    localStorage.removeItem('loggedInUserId'); // Usuń ID użytkownika
    this.role = null; // Resetowanie roli
    this.router.navigate(['/login']); // Przekierowanie na stronę logowania
    console.log('User logged out');
  }

  navigateToRegister(): void {
    this.router.navigate(['/register']);
  }

  navigateToLogin(): void {
    this.router.navigate(['/login']);
  }
}
