import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { LoginComponent } from './pages/login.component/login.component';
import { DashboardComponent } from './pages/dashboard.component/dashboard.component';
import { CustomeroverviewComponent } from './pages/customeroverview.component/customeroverview.component';
import { CustomerdetailComponent } from './pages/customerdetail.component/customerdetail.component';
import { BulkActionsComponent } from './pages/bulk-actions.component/bulk-actions.component';
import { AuditLogComponent } from './pages/audit-log.component/audit-log.component';
import { SegmentsComponents } from './pages/segments.components/segments.components';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  { path: 'login', component: LoginComponent },

  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'customers', component: CustomeroverviewComponent, canActivate: [authGuard] },
  { path: 'customers/bulk-actions', component: BulkActionsComponent, canActivate: [authGuard] },
  { path: 'customers/:id', component: CustomerdetailComponent, canActivate: [authGuard] },
  { path: 'segments', component: SegmentsComponents, canActivate: [authGuard] },
  { path: 'audit-log', component: AuditLogComponent, canActivate: [authGuard] },

  { path: '**', redirectTo: 'login' },
];