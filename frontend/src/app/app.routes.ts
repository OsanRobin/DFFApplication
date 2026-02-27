import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login.component/login.component';
import { DashboardComponent } from './pages/dashboard.component/dashboard.component';
import { CustomeroverviewComponent } from './pages/customeroverview.component/customeroverview.component';
import { CustomerdetailComponent } from './pages/customerdetail.component/customerdetail.component';
import { AttributeoverviewComponent } from './pages/attributeoverview.component/attributeoverview.component';
import { SegmentsComponent } from './pages/segments.component/segments.component';

export const routes: Routes = [
     { path: '', redirectTo: 'login', pathMatch: 'full' },

  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent },
   { path: 'customers', component: CustomeroverviewComponent },
   {path: 'customers/:id', component: CustomerdetailComponent },
{ path: 'attributes', component: AttributeoverviewComponent },
{ path: 'segments', component: SegmentsComponent },

  { path: '**', redirectTo: 'login' }
];
