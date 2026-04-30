import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SelectedCustomerRow {
  id: string;
  name: string;
  customerNo: string;
  type: string;
  status: string;
  locations: number;
  level?: number;
}

export interface BulkActionRequest {
  customerIds: string[];
  action: string;
  attributeName?: string;
  attributeValue?: string;
  segmentId?: string;
}

export interface BulkActionResponse {
  success: boolean;
  message: string;
  processedCount: number;
  processedCustomerIds: string[];
  errors: string[];
}

export interface AttributeOption {
  name: string;
  displayName: string;
}

export interface SegmentOption {
  id: string;
  name: string;
  description?: string;
}

@Injectable({
  providedIn: 'root'
})
export class BulkSelectionService {
  private readonly storageKey = 'bulkSelectedCustomers';
  private readonly bulkActionsUrl = 'http://localhost:8081/api/bulk-actions';
  private readonly attributesUrl = 'http://localhost:8081/api/bulk-actions/attributes';
  private readonly segmentsUrl = 'http://localhost:8081/api/bulk-actions/segments';

  private selectedCustomers: SelectedCustomerRow[] = [];
  private http = inject(HttpClient);

  constructor() {
    this.loadFromStorage();
  }

  setSelectedCustomers(customers: SelectedCustomerRow[]): void {
    this.selectedCustomers = [...customers];
    this.saveToStorage();
  }

  getSelectedCustomers(): SelectedCustomerRow[] {
    return [...this.selectedCustomers];
  }

  getSelectedCustomerIds(): string[] {
    return this.selectedCustomers.map(customer => customer.id);
  }

  getCount(): number {
    return this.selectedCustomers.length;
  }

  hasSelection(): boolean {
    return this.selectedCustomers.length > 0;
  }

  isSelected(customerId: string): boolean {
    return this.selectedCustomers.some(customer => customer.id === customerId);
  }

  addCustomer(customer: SelectedCustomerRow): void {
    if (!this.isSelected(customer.id)) {
      this.selectedCustomers.push(customer);
      this.saveToStorage();
    }
  }

  removeCustomer(customerId: string): void {
    this.selectedCustomers = this.selectedCustomers.filter(
      customer => customer.id !== customerId
    );
    this.saveToStorage();
  }

  clear(): void {
    this.selectedCustomers = [];
    sessionStorage.removeItem(this.storageKey);
  }

  executeBulkAction(request: BulkActionRequest): Observable<BulkActionResponse> {
    return this.http.post<BulkActionResponse>(this.bulkActionsUrl, request, {
      withCredentials: true
    });
  }

  getAvailableAttributes(): Observable<AttributeOption[]> {
    return this.http.get<AttributeOption[]>(this.attributesUrl, {
      withCredentials: true
    });
  }

  getAvailableSegments(): Observable<SegmentOption[]> {
    return this.http.get<SegmentOption[]>(this.segmentsUrl, {
      withCredentials: true
    });
  }

  private saveToStorage(): void {
    sessionStorage.setItem(this.storageKey, JSON.stringify(this.selectedCustomers));
  }

  private loadFromStorage(): void {
    const storedValue = sessionStorage.getItem(this.storageKey);

    if (!storedValue) {
      this.selectedCustomers = [];
      return;
    }

    try {
      this.selectedCustomers = JSON.parse(storedValue) as SelectedCustomerRow[];
    } catch {
      this.selectedCustomers = [];
      sessionStorage.removeItem(this.storageKey);
    }
  }
}