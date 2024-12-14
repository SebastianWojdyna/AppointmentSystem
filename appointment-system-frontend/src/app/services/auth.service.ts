import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private loginUrl = 'http://localhost:8080/api/auth/login';

  constructor(private http: HttpClient, private router: Router) {}

  login(credentials: { email: string; password: string }): void {
    this.http.post<{ token: string; role: string }>(this.loginUrl, credentials).subscribe({
      next: (response) => {
        // Zapisz token w localStorage
        localStorage.setItem('token', response.token);
        // Przekierowanie użytkownika w zależności od roli
        if (response.role === 'ADMIN') {
          this.router.navigate(['/admin/dashboard']);
        } else if (response.role === 'PATIENT') {
          this.router.navigate(['/patient/dashboard']);
        } else if (response.role === 'DOCTOR') {
          this.router.navigate(['/doctor/dashboard']);
        } else if (response.role === 'RECEPTIONIST') {
          this.router.navigate(['/receptionist/dashboard']);
        } else {
          console.error('Nieznana rola użytkownika');
        }
      },
      error: (err) => {
        console.error('Login failed:', err);
      }
    });
  }

  logout(): void {
    localStorage.removeItem('token');
    this.router.navigate(['/login']);
  }
}
