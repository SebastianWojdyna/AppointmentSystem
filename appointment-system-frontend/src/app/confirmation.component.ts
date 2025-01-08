import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
    selector: 'app-confirmation',
    templateUrl: './confirmation.component.html',
    styleUrls: ['./confirmation.component.css'],
    standalone: false
})
export class ConfirmationComponent implements OnInit {
  orderId: string = '';

  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.orderId = params['orderId'];
      if (!this.orderId) {
        console.error('Order ID is missing in the URL parameters');
      }

      // Przekierowanie po 4 sekundach
      setTimeout(() => {
        this.router.navigate(['/home']);
      }, 4000);
    });
  }
}
