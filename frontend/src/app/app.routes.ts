import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login.component/login.component';
import { DashboardComponent } from './pages/dashboard.component/dashboard.component';
import { CustomeroverviewComponent } from './pages/customeroverview.component/customeroverview.component';

export const routes: Routes = [
     { path: '', redirectTo: 'login', pathMatch: 'full' },

  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent },
   { path: 'customers', component: CustomeroverviewComponent },

  { path: '**', redirectTo: 'login' }
];
