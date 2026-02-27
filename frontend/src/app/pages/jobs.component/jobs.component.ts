import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { HeaderComponent } from '../../shell/header.component/header.component';
type JobStatus = 'Completed' | 'Running' | 'Failed' | 'Queued';

type JobRow = {
  id: string;
  type: string;
  triggeredBy: string;
  date: string;
  status: JobStatus;
};
@Component({
  selector: 'app-jobs.component',
  imports: [CommonModule, HeaderComponent],
  templateUrl: './jobs.component.html',
  styleUrl: './jobs.component.css',
})
export class JobsComponent {
  jobs: JobRow[] = [
    {
      id: 'J001',
      type: 'Bulk Attribute Update',
      triggeredBy: 'admin@intershop.com',
      date: '2026-02-20 09:30:00',
      status: 'Completed'
    },
    {
      id: 'J002',
      type: 'Segment Assignment',
      triggeredBy: 'auth.manager@intershop.com',
      date: '2026-02-20 09:15:00',
      status: 'Running'
    },
    {
      id: 'J003',
      type: 'Add Attribute',
      triggeredBy: 'admin@intershop.com',
      date: '2026-02-20 08:45:00',
      status: 'Failed'
    },
    {
      id: 'J004',
      type: 'Update Attribute',
      triggeredBy: 'admin@intershop.com',
      date: '2026-02-19 16:20:00',
      status: 'Completed'
    },
    {
      id: 'J005',
      type: 'Bulk Attribute Update',
      triggeredBy: 'auth.manager@intershop.com',
      date: '2026-02-19 14:00:00',
      status: 'Queued'
    }
  ];

  statusClass(status: JobStatus): string {
    if (status === 'Completed') return 'pill status completed';
    if (status === 'Running') return 'pill status running';
    if (status === 'Failed') return 'pill status failed';
    return 'pill status queued';
  }

  onViewDetails(_job: JobRow): void {}

}
