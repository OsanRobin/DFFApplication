import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SegmentCard {
  id: string;
  name: string;
  description: string | null;
  rule: string | null;
  matchedCustomers: number;
  lastUpdated: string;
  autoUpdated: boolean;
}

export interface LogItem {
  id: string;
  direction: 'up' | 'down';
  message: string;
  timestamp: string;
}

export interface CreateSegmentRequest {
  name: string;
  description: string;
  rule: string;
}

@Injectable({
  providedIn: 'root'
})
export class SegmentsApiService {
  private http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8081/api/segments';

  getSegments(): Observable<SegmentCard[]> {
    return this.http.get<SegmentCard[]>(this.baseUrl);
  }

  getLogItems(): Observable<LogItem[]> {
    return this.http.get<LogItem[]>(`${this.baseUrl}/log`);
  }

  syncSegments(): Observable<{ success: boolean; importedCount: number }> {
    return this.http.post<{ success: boolean; importedCount: number }>(`${this.baseUrl}/sync`, {});
  }

  updateSegmentRule(segmentId: string, rule: string): Observable<{ success: boolean; message: string }> {
    return this.http.put<{ success: boolean; message: string }>(
      `${this.baseUrl}/${segmentId}/rule`,
      { rule }
    );
  }

  createSegment(data: CreateSegmentRequest): Observable<{ success: boolean; id: string }> {
    return this.http.post<{ success: boolean; id: string }>(this.baseUrl, data);
  }
}