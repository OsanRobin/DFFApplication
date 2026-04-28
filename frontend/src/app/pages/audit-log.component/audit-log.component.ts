import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { HeaderComponent } from '../../shell/header.component/header.component';
import {
  AuditLogApiService,
  AuditLogDto,
} from '../../core/api/audit-log-api.service';

@Component({
  selector: 'app-audit-log.component',
  imports: [CommonModule, HeaderComponent],
  templateUrl: './audit-log.component.html',
  styleUrl: './audit-log.component.css',
})
export class AuditLogComponent implements OnInit {
  private auditLogApi = inject(AuditLogApiService);

  auditLogs: AuditLogDto[] = [];

  isLoading = false;
  errorMessage = '';

  ngOnInit(): void {
    this.loadAuditLogs();
  }

  loadAuditLogs(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.auditLogApi.getAuditLogs().subscribe({
      next: logs => {
        this.auditLogs = logs;
        this.isLoading = false;
      },
      error: error => {
        console.error(error);
        this.errorMessage = 'Could not load audit logs.';
        this.isLoading = false;
      },
    });
  }

  auditActionClass(action: string): string {
    if (action === 'CREATE') return 'pill audit create';
    if (action === 'UPDATE') return 'pill audit update';
    if (action === 'DELETE') return 'pill audit delete';
    if (action === 'BULK_CREATE') return 'pill audit bulk';
    if (action === 'BULK_UPDATE') return 'pill audit bulk';

    return 'pill audit default';
  }

  formatDate(value: string): string {
    if (!value) return '-';
    return value.replace('T', ' ').slice(0, 19);
  }

  displayValue(value: string | null | undefined): string {
    if (value == null || value === '') return '-';
    return value;
  }
}