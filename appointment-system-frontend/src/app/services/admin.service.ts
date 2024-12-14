import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = 'http://localhost:8080/api/admin/dashboard';

  constructor(private http: HttpClient) {
    console.log('AdminService initialized');
  }

  getAdminDashboard(): Observable<string> {
    console.log('AdminService: Sending request to', this.apiUrl);
    return this.http.get<string>(this.apiUrl);
  }
}
