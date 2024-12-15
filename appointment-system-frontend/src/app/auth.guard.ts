import { Injectable } from '@angular/core';
import {
  CanActivate,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  UrlTree,
  Router,
} from '@angular/router';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {
  constructor(private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | UrlTree {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');

    if (!token) {
      console.warn('Brak tokena, przekierowanie na stronę logowania');
      return this.router.createUrlTree(['/login']);
    }

    // Sprawdź, czy wymagane role są zgodne z rolą użytkownika
    const requiredRoles: string[] = route.data['roles'];
    if (requiredRoles && !requiredRoles.includes(role!)) {
      console.warn('Brak dostępu dla roli:', role);
      return this.router.createUrlTree(['/home']);
    }

    return true; // Użytkownik ma dostęp
  }
}
