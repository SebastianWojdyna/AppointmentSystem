import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  users: any[] = []; // Lista użytkowników
  roles: string[] = ['ADMIN', 'DOCTOR', 'RECEPTIONIST', 'PATIENT']; // Możliwe role
  successMessage: string = ''; // Wiadomość o sukcesie
  errorMessage: string = ''; // Wiadomość o błędzie
  newUser: any = { username: '', email: '', password: '', role: '' }; // Nowy użytkownik
  updatedUser: any = { id: null, username: '', email: '', role: '' }; // Użytkownik do edycji

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  // Wyczyszczenie komunikatów
  clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }

  // Ładowanie użytkowników
  loadUsers(): void {
    this.clearMessages();
    this.http.get<any>('http://localhost:8080/api/admin/users').subscribe({
      next: (response) => {
        this.users = response.content || []; // Wczytaj użytkowników
      },
      error: (err) => {
        console.error('Błąd ładowania użytkowników:', err);
        this.errorMessage = 'Nie udało się załadować listy użytkowników.';
      }
    });
  }

  // Dodanie nowego użytkownika
  addUser(): void {
    this.clearMessages();
    if (!this.newUser.password) {
      this.errorMessage = 'Hasło jest wymagane!';
      return;
    }
    this.http.post('http://localhost:8080/api/admin/users', this.newUser).subscribe({
      next: () => {
        this.successMessage = 'Użytkownik dodany pomyślnie!';
        this.newUser = { username: '', email: '', password: '', role: '' };
        this.loadUsers();
      },
      error: (err) => {
        console.error('Błąd dodawania użytkownika:', err);
        this.errorMessage = 'Nie udało się dodać użytkownika.';
      }
    });
  }

  // Zmiana roli użytkownika
  changeUserRole(userId: number, newRole: string): void {
    this.clearMessages();
    this.http.patch(`http://localhost:8080/api/admin/users/${userId}/role`, { role: newRole }).subscribe({
      next: () => {
        this.successMessage = `Rola użytkownika została zmieniona na ${newRole}.`;
        this.loadUsers();
      },
      error: (err) => {
        console.error('Błąd zmiany roli:', err);
        this.errorMessage = 'Nie udało się zmienić roli użytkownika.';
      }
    });
  }

  // Ustawienie użytkownika do edycji
  setUpdatedUser(user: any): void {
    this.updatedUser = { id: user.id, username: user.username, email: user.email, role: user.role };
  }

  // Aktualizacja użytkownika
  updateUser(): void {
    this.clearMessages();
    this.http.put(`http://localhost:8080/api/admin/users/${this.updatedUser.id}`, this.updatedUser).subscribe({
      next: () => {
        this.successMessage = 'Dane użytkownika zostały zaktualizowane.';
        this.updatedUser = { id: null, username: '', email: '', role: '' };
        this.loadUsers();
      },
      error: (err) => {
        console.error('Błąd aktualizacji użytkownika:', err);
        this.errorMessage = 'Nie udało się zaktualizować użytkownika.';
      }
    });
  }

  // Usunięcie użytkownika
  deleteUser(userId: number): void {
    this.clearMessages();
    if (confirm('Czy na pewno chcesz usunąć tego użytkownika?')) {
      this.http.delete(`http://localhost:8080/api/admin/users/${userId}`).subscribe({
        next: () => {
          this.successMessage = 'Użytkownik został usunięty.';
          this.loadUsers();
        },
        error: (err) => {
          console.error('Błąd usuwania użytkownika:', err);
          this.errorMessage = 'Nie udało się usunąć użytkownika.';
        }
      });
    }
  }
}
