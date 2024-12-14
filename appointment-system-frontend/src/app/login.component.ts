import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;

  constructor(private fb: FormBuilder, private authService: AuthService) {}

  ngOnInit(): void {
    // Inicjalizacja formularza w metodzie ngOnInit
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]], // Walidacja pola email
      password: ['', [Validators.required, Validators.minLength(6)]] // Walidacja hasła
    });
  }

  login(): void {
    if (this.loginForm.valid) {
      const credentials = this.loginForm.value; // Pobierz dane z formularza
      this.authService.login(credentials);
    } else {
      console.error('Form is invalid');
    }
  }
}
