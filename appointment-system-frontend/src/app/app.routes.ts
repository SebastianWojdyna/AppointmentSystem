import { Routes } from '@angular/router';
import { AppComponent } from './app.component';
import { ConfirmationComponent } from './confirmation.component';

export const routes: Routes = [
  { path: '', component: AppComponent },
  { path: 'confirmation', component: ConfirmationComponent }
];
