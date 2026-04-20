import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { LoginComponent } from './pages/login.component/login.component';
import { DashboardComponent } from './pages/dashboard.component/dashboard.component';
import { CustomeroverviewComponent } from './pages/customeroverview.component/customeroverview.component';
import { CustomerdetailComponent } from './pages/customerdetail.component/customerdetail.component';
import { JobsComponent } from './pages/jobs.component/jobs.component';
import { BulkActionsComponent } from './pages/bulk-actions.component/bulk-actions.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  { path: 'login', component: LoginComponent },

  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'customers', component: CustomeroverviewComponent, canActivate: [authGuard] },
  { path: 'customers/bulk-actions', component: BulkActionsComponent, canActivate: [authGuard] },
  { path: 'customers/:id', component: CustomerdetailComponent, canActivate: [authGuard] },
  { path: 'jobs', component: JobsComponent, canActivate: [authGuard] },

  { path: '**', redirectTo: 'login' }
];