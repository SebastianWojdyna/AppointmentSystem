import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./login.component.css']
})
export class RegisterComponent {
  user = { username: '', email: '', password: '' };

  constructor(private http: HttpClient, private router: Router) {}

  register() {
    this.http.post('http://localhost:8080/api/users/register', this.user).subscribe((response: any) => {
      alert('Rejestracja zakończona sukcesem');
      this.router.navigate(['/login']);
    }, error => {
      console.error('Błąd podczas rejestracji', error);
    });
  }

  navigateToLogin() {
    this.router.navigate(['/login']);
  }
}
