import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './login.component';
import { RegisterComponent } from './register.component';
import { HomeComponent } from './home.component';
import { ConfirmationComponent } from './confirmation.component';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { DoctorDashboardComponent } from './components/doctor-dashboard/doctor-dashboard.component';
import { ReceptionistDashboardComponent } from './components/receptionist-dashboard/receptionist-dashboard.component';
import { PatientDashboardComponent } from './components/patient-dashboard/patient-dashboard.component';
import { AuthGuard } from './auth.guard';
import { ReservedAppointmentsComponent } from './components/reserved-appointments/reserved-appointments.component';

const routes: Routes = [
  { path: 'reserve', component: PatientDashboardComponent },  // Zarezerwuj wizytę
  { path: 'reserved', component: ReservedAppointmentsComponent },  // Twoje zarezerwowane wizyty
  { path: '', redirectTo: '/reserve', pathMatch: 'full' },  // Domyślnie przekierowuje na Zarezerwuj wizytę
  { path: '**', redirectTo: '/reserve' },  // Przekierowanie na /reserve jeśli ścieżka jest błędna
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  {
    path: 'home',
    component: HomeComponent,
    canActivate: [AuthGuard],
    data: { roles: ['PATIENT', 'ADMIN'] } // Dostęp dla PACJENTA i ADMINA
  },
  { path: 'confirmation', component: ConfirmationComponent },
  {
    path: 'admin/dashboard',
    component: AdminDashboardComponent,
    canActivate: [AuthGuard],
    data: { roles: ['ADMIN'] } // Dostęp tylko dla ADMINA
  },
  { path: 'doctor/dashboard',
    component: DoctorDashboardComponent,
    canActivate: [AuthGuard],
    data: { roles: ['DOCTOR'] }
  },
  { path: 'receptionist/dashboard',
    component: ReceptionistDashboardComponent,
    canActivate: [AuthGuard],
    data: { roles: ['RECEPTIONIST'] }
  },
  { path: 'patient/dashboard',
    component: PatientDashboardComponent,
    canActivate: [AuthGuard],
    data: { roles: ['PATIENT'] }
  },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
