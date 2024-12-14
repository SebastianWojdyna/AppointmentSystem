import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private router: Router) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Pobierz token z localStorage
    const token = localStorage.getItem('token');
    console.log('AuthInterceptor: Token found:', token);

    // Jeśli token istnieje, dodaj nagłówek Authorization
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    // Obsługa odpowiedzi i błędów
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        // Obsługa błędów 401 (Unauthorized) i 403 (Forbidden)
        if (error.status === 401) {
          console.warn('Unauthorized request detected:', error.message);
          localStorage.removeItem('token'); // Usuń token
          this.router.navigate(['/login']); // Przekieruj na stronę logowania
        } else if (error.status === 403) {
          console.warn('Forbidden request detected:', error.message);
          // Przekieruj na stronę główną lub wyświetl komunikat o braku dostępu
          this.router.navigate(['/home']);
        }

        // Przełącz obsługę błędu dalej
        return throwError(() => error);
      })
    );
  }
}
