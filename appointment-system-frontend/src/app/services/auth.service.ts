import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private loginUrl = 'https://appointment-system-backend.azurewebsites.net/api/auth/login';

  constructor(private http: HttpClient) {}

  // Funkcja logowania
  login(credentials: { email: string; password: string }): Observable<any> {
    return this.http.post<any>(this.loginUrl, credentials);
  }

  // Funkcja wylogowania
  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('email');
    localStorage.removeItem('role');
  }

  // Pobierz aktualną rolę użytkownika
  getRole(): string | null {
    return localStorage.getItem('role');
  }

  // Pobierz aktualny email użytkownika
  getEmail(): string | null {
    return localStorage.getItem('email');
  }
}
