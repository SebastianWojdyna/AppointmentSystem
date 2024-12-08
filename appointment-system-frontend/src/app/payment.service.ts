import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {

  private apiUrl = 'http://localhost:8080/api/payments';

  constructor(private http: HttpClient) { }

  createAppointment(appointment: any) {
    return this.http.post(`${this.apiUrl}/create-appointment`, appointment);
  }

  processPayment(orderId: string) {
    return this.http.post(`${this.apiUrl}/process`, { orderId });
  }

  sendNotification(orderId: string, success: boolean) {
    return this.http.post(`${this.apiUrl}/send-notification`, { orderId, success });
  }
}
