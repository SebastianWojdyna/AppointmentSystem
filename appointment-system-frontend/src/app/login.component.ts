import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from './services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]], // Obsługa pola email
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  login(): void {
    if (this.loginForm.valid) {
      const credentials = this.loginForm.value; // Pobierz email i hasło z formularza

      this.authService.login(credentials).subscribe({
        next: (response) => {
          console.log('Logowanie pomyślne:', response);

          // Zapisz token, email i rolę w localStorage
          localStorage.setItem('token', response.token);
          localStorage.setItem('email', response.email); // Email użytkownika
          localStorage.setItem('role', response.role);

          // Przekierowanie na podstawie roli użytkownika
          if (response.role === 'ADMIN') {
            this.router.navigate(['/admin/dashboard']);
          } else if (response.role === 'PATIENT') {
            this.router.navigate(['/home']);
          } else if (response.role === 'DOCTOR') {
            this.router.navigate(['/doctor/dashboard']); // W przyszłości możemy dodać widok dla lekarza
          } else {
            console.error('Nieznana rola:', response.role);
            this.router.navigate(['/login']); // Powrót do logowania w przypadku błędu
          }
        },
        error: (err) => {
          console.error('Błąd logowania:', err);
        },
      });
    } else {
      console.error('Formularz logowania jest nieprawidłowy');
    }
  }
}
