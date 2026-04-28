import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type AuditAction =
  | 'CREATE'
  | 'UPDATE'
  | 'DELETE'
  | 'BULK_UPDATE'
  | 'BULK_CREATE';

export type AuditLogDto = {
  id: number;
  entityType: string;
  entityId: string;
  action: AuditAction | string;
  fieldName: string;
  oldValue: string;
  newValue: string;
  changedBy: string;
  changedAt: string;
  message: string;
};

@Injectable({
  providedIn: 'root',
})
export class AuditLogApiService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8081/api/audit-logs';

  getAuditLogs(): Observable<AuditLogDto[]> {
    return this.http.get<AuditLogDto[]>(this.baseUrl);
  }
}