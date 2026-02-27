import { Component } from '@angular/core';
type Activity = {
  title: string;
  by: string;
  at: string;
};
@Component({
  selector: 'app-dashboard.component',
  imports: [],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent {
   activities: Activity[] = [
    { title: 'Bulk Attribute Update', by: 'admin@intershop.com', at: '2026-02-20 09:30:00' },
    { title: 'Segment Assignment', by: 'auth.manager@intershop.com', at: '2026-02-20 09:15:00' },
    { title: 'Add Attribute', by: 'admin@intershop.com', at: '2026-02-20 08:45:00' },
  ];

}
