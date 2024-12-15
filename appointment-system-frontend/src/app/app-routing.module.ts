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

const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'home', component: HomeComponent },
  { path: 'confirmation', component: ConfirmationComponent },
  { path: 'admin/dashboard', component: AdminDashboardComponent },
  { path: 'doctor/dashboard', component: DoctorDashboardComponent },
  { path: 'receptionist/dashboard', component: ReceptionistDashboardComponent },
  { path: 'patient/dashboard', component: PatientDashboardComponent },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
