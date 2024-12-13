import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';


@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  loginForm: FormGroup;

  constructor(private fb: FormBuilder, private http: HttpClient, private router: Router) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  login() {
    if (this.loginForm.valid) {
      this.http.post<any>('http://localhost:8080/api/auth/login', this.loginForm.value)
        .subscribe({
          next: (response) => {
            console.log(response.message);
            console.log(response.token);
            console.log(response.userId);
            // Save token and user ID in localStorage
            localStorage.setItem('token', response.token);
            localStorage.setItem('loggedInUserId', response.userId);
            localStorage.setItem('loggedInUser', this.loginForm.value.email);
            alert('Zalogowano pomyślnie!');
            setTimeout(() => {
              this.router.navigate(['/home']);
            }, 100);
          },
          error: (error) => {
            console.error('There was an error!', error);
          }
        });
    }
  }
}
