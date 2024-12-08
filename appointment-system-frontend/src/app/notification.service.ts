import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = 'http://localhost:8080/api/notifications';

  constructor(private http: HttpClient) { }

  sendNotification(orderId: string, paid: boolean) {
    const payload = {
      orderId: orderId,
      paid: paid
    };
    console.log('Wysyłanie powiadomienia:', payload);
    return this.http.post(this.apiUrl, payload).subscribe(
      response => console.log('Powiadomienie wysłane pomyślnie', response),
      error => console.error('Błąd podczas wysyłania powiadomienia', error)
    );
  }
}
