import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { RegisterComponent } from './register.component';
import { LoginComponent } from './login.component';
import { HomeComponent } from './home.component';
import { ConfirmationComponent } from './confirmation.component';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { DoctorDashboardComponent } from './components/doctor-dashboard/doctor-dashboard.component';
import { ReceptionistDashboardComponent } from './components/receptionist-dashboard/receptionist-dashboard.component';
import { PatientDashboardComponent } from './components/patient-dashboard/patient-dashboard.component';
import { ReactiveFormsModule } from '@angular/forms';
import { HashLocationStrategy, LocationStrategy } from '@angular/common';

@NgModule({
  declarations: [
    AppComponent,
    RegisterComponent,
    LoginComponent,
    HomeComponent,
    ConfirmationComponent,
    AdminDashboardComponent,
    DoctorDashboardComponent,
    ReceptionistDashboardComponent,
    PatientDashboardComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    AppRoutingModule,
    ReactiveFormsModule,
  ],
  providers: [
  { provide: LocationStrategy, useClass: HashLocationStrategy }
],
  bootstrap: [AppComponent]
})
export class AppModule {}
