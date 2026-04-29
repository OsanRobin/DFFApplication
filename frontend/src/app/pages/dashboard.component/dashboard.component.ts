import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';

import { HeaderComponent } from '../../shell/header.component/header.component';
import { CustomerApiService, CustomerDto } from '../../core/api/customer-api.service';
import {
  AuditLogApiService,
  AuditLogDto,
} from '../../core/api/audit-log-api.service';
import { AuthService } from '../../core/auth/auth.service';

type Activity = {
  title: string;
  by: string;
  at: string;
  type: 'Customer' | 'Audit';
};

@Component({
  selector: 'app-dashboard.component',
  imports: [CommonModule, RouterLink, HeaderComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent implements OnInit {
  private customerApi = inject(CustomerApiService);
  private auditLogApi = inject(AuditLogApiService);
  private authService = inject(AuthService);

  private readonly domainName = 'DailyFreshFood-B1-Anonymous';

  isLoading = false;
  errorMessage = '';

  totalCustomers = 0;
  activeCustomers = 0;
  auditEvents = 0;
  latestActivity = '-';

  activities: Activity[] = [];

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    const authenticationToken = this.authService.getAuthenticationToken();

    if (!authenticationToken) {
      this.errorMessage = 'No authentication token found. Please log in first.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    forkJoin({
      customers: this.customerApi
        .getCustomers(authenticationToken, this.domainName, 0, 1500)
        .pipe(
          catchError(error => {
            console.error(error);
            this.errorMessage = 'Customers could not be loaded.';
            return of(null);
          })
        ),

      auditLogs: this.auditLogApi.getAuditLogs().pipe(
        catchError(error => {
          console.error(error);
          this.errorMessage = this.errorMessage
            ? `${this.errorMessage} Audit log could not be loaded.`
            : 'Audit log could not be loaded.';
          return of([]);
        })
      ),
    }).subscribe({
      next: result => {
        const customers = result.customers?.data ?? [];
        const auditLogs = result.auditLogs ?? [];

        this.totalCustomers = result.customers?.count ?? customers.length;
        this.activeCustomers = customers.filter(customer => this.isActiveCustomer(customer)).length;

        this.auditEvents = auditLogs.length;
        this.latestActivity = this.getLatestActivityTime(auditLogs);

        this.activities = auditLogs
          .slice(0, 5)
          .map(log => this.mapAuditLogToActivity(log));

        this.isLoading = false;
      },
      error: error => {
        console.error(error);
        this.errorMessage = 'Dashboard could not be loaded.';
        this.isLoading = false;
      },
    });
  }

  private isActiveCustomer(customer: CustomerDto): boolean {
    return customer.active === true;
  }

  private mapAuditLogToActivity(log: AuditLogDto): Activity {
    return {
      title: log.message || `${log.action} ${log.fieldName || log.entityType}`,
      by: this.displayValue(log.changedBy),
      at: this.formatDate(log.changedAt),
      type: log.entityType?.toUpperCase().includes('CUSTOMER') ? 'Customer' : 'Audit',
    };
  }

  private getLatestActivityTime(logs: AuditLogDto[]): string {
    if (!logs.length || !logs[0].changedAt) {
      return '-';
    }

    return this.formatTime(logs[0].changedAt);
  }

  formatDate(value: string): string {
    if (!value) return '-';
    return value.replace('T', ' ').slice(0, 19);
  }

  private formatTime(value: string): string {
    if (!value) return '-';
    return value.replace('T', ' ').slice(11, 16);
  }

  displayValue(value: string | null | undefined): string {
    if (value == null || value === '') return '-';
    return value;
  }
}