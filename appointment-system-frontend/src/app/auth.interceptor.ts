import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private router: Router) {}

  intercept(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    const token = localStorage.getItem('token');
    console.log('AuthInterceptor: Token found in localStorage:', token);

    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      });
      console.log('AuthInterceptor: Authorization header set:', request.headers.get('Authorization'));
    } else {
      console.warn('AuthInterceptor: No token found in localStorage');
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        console.error('AuthInterceptor: HTTP error:', error.status, error.message);
        if (error.status === 401) {
          localStorage.removeItem('token');
          this.router.navigate(['/login']);
        } else if (error.status === 403) {
          this.router.navigate(['/home']);
        }
        return throwError(() => error);
      })
    );
  }
}
