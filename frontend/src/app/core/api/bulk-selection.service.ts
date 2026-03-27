import { Injectable } from '@angular/core';

export interface SelectedCustomerRow {
  id: string;
  name: string;
  customerNo: string;
  type: string;
  status: string;
  locations: number;
  level?: number;
}

@Injectable({
  providedIn: 'root'
})
export class BulkSelectionService {
  private selectedCustomers: SelectedCustomerRow[] = [];

  setSelectedCustomers(customers: SelectedCustomerRow[]): void {
    this.selectedCustomers = [...customers];
  }

  getSelectedCustomers(): SelectedCustomerRow[] {
    return [...this.selectedCustomers];
  }

  clear(): void {
    this.selectedCustomers = [];
  }
}