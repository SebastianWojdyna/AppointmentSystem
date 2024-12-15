import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  user = { username: '', email: '', password: '' }; // Dane użytkownika

  constructor(private http: HttpClient, private router: Router) {}

  // Metoda do obsługi rejestracji
  register() {
    this.http.post('http://localhost:8080/api/auth/register', this.user).subscribe(
      (response: any) => {
        alert('Rejestracja zakończona sukcesem. Możesz się teraz zalogować.');
        this.router.navigate(['/login']);
      },
      (error) => {
        console.error('Błąd podczas rejestracji:', error);

        // Sprawdź, czy serwer zwraca komunikat o błędzie w JSON
        let errorMessage = 'Coś poszło nie tak!';

        if (error.error) {
          if (typeof error.error === 'string') {
            // Jeśli serwer zwraca komunikat jako string
            errorMessage = error.error;
          } else if (error.error.message) {
            // Jeśli serwer zwraca komunikat w polu "message"
            errorMessage = error.error.message;
          } else if (typeof error.error === 'object') {
            // Jeśli serwer zwraca obiekt z innymi szczegółami
            errorMessage = JSON.stringify(error.error);
          }
        }

        alert(`Błąd rejestracji: ${errorMessage}`);
      }
    );
  }

  navigateToLogin() {
    this.router.navigate(['/login']);
  }
}
