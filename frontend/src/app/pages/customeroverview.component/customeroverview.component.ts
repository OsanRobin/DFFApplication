import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HeaderComponent } from '../../shell/header.component/header.component';
import {
  CustomerApiService,
  CustomerDto,
  SavedCustomerSearchDto,
  SaveCustomerSearchRequest
} from '../../core/api/customer-api.service';
import { AuthService } from '../../core/auth/auth.service';

type Status = 'Active' | 'Inactive';

type CustomerRow = {
  id: string;
  name: string;
  customerNo: string;
  type: string;
  segment: string;
  status: Status;
  locations: number;
  level: 0 | 1;
  parentId?: string;
};

@Component({
  selector: 'app-customeroverview.component',
  imports: [CommonModule, FormsModule, RouterModule, HeaderComponent],
  templateUrl: './customeroverview.component.html',
  styleUrl: './customeroverview.component.css',
})
export class CustomeroverviewComponent {
  private customerApi = inject(CustomerApiService);
  private authService = inject(AuthService);
  private router = inject(Router);

  private domainName = 'DailyFreshFood-B1-Anonymous';

  query = '';
  customerNoFilter = '';
  typeFilter = '';
  statusFilter = '';

  bulkOpen = false;
  selectedIds = new Set<string>();

  loading = false;
  error = '';
  saveSearchError = '';
  saveSearchSuccess = '';

  rows: CustomerRow[] = [];
  savedSearches: SavedCustomerSearchDto[] = [];

  constructor() {
    this.loadCustomers();
    this.loadSavedSearches();
  }

  loadCustomers(): void {
    const authenticationToken = this.authService.getAuthenticationToken();

    if (!authenticationToken) {
      this.error = 'No authentication token found. Please log in first.';
      return;
    }

    this.loading = true;
    this.error = '';

    this.customerApi.getCustomers(
      authenticationToken,
      this.domainName,
      0,
      100,
      this.customerNoFilter
    ).subscribe({
      next: (response) => {
        this.rows = (response.data ?? []).map(customer => this.mapCustomerToRow(customer));
        this.loading = false;
      },
      error: (err) => {
        console.error(err);

        if (err.status === 401 || err.status === 403) {
          this.error = 'Your session expired. Please log in again.';
          this.loading = false;
          this.router.navigate(['/login']);
          return;
        }

        this.error = 'Failed to load customers.';
        this.loading = false;
      }
    });
  }

  loadSavedSearches(): void {
    const authenticationToken = this.authService.getAuthenticationToken();

    if (!authenticationToken) {
      return;
    }

    this.customerApi.getSavedSearches(authenticationToken, this.domainName).subscribe({
      next: (response) => {
        this.savedSearches = response.data ?? [];
      },
      error: (err) => {
        console.error('Failed to load saved searches', err);
      }
    });
  }

  saveCurrentSearch(): void {
    const authenticationToken = this.authService.getAuthenticationToken();

    if (!authenticationToken) {
      this.saveSearchError = 'No authentication token found. Please log in first.';
      return;
    }

    this.saveSearchError = '';
    this.saveSearchSuccess = '';

    const name = window.prompt('Enter a name for this search');
    if (!name || !name.trim()) {
      this.saveSearchError = 'Search name is required.';
      return;
    }

    const request: SaveCustomerSearchRequest = {
      domainName: this.domainName,
      name: name.trim(),
      query: this.query.trim(),
      customerNo: this.customerNoFilter.trim(),
      typeFilter: this.typeFilter,
      statusFilter: this.statusFilter,
      overwrite: false
    };

    this.customerApi.saveSearch(authenticationToken, request).subscribe({
      next: () => {
        this.saveSearchSuccess = 'Search saved successfully.';
        this.loadSavedSearches();
      },
      error: (err) => {
        const backendMessage = err?.error?.message ?? '';

        if (backendMessage.toLowerCase().includes('already exists')) {
          const overwrite = window.confirm('A saved search with this name already exists. Overwrite it?');

          if (!overwrite) {
            this.saveSearchError = 'Search was not overwritten.';
            return;
          }

          this.customerApi.saveSearch(authenticationToken, {
            ...request,
            overwrite: true
          }).subscribe({
            next: () => {
              this.saveSearchSuccess = 'Search overwritten successfully.';
              this.loadSavedSearches();
            },
            error: (overwriteErr) => {
              console.error(overwriteErr);
              this.saveSearchError = overwriteErr?.error?.message ?? 'Failed to overwrite saved search.';
            }
          });

          return;
        }

        console.error(err);
        this.saveSearchError = backendMessage || 'Failed to save search.';
      }
    });
  }

  applySavedSearch(savedSearch: SavedCustomerSearchDto): void {
    this.query = savedSearch.query ?? '';
    this.customerNoFilter = savedSearch.customerNo ?? '';
    this.typeFilter = savedSearch.typeFilter ?? '';
    this.statusFilter = savedSearch.statusFilter ?? '';

    this.onApplyFilters();
  }

  mapCustomerToRow(customer: CustomerDto): CustomerRow {
    return {
      id: customer.id,
      name: customer.displayName || customer.companyName || customer.customerNo || 'Unnamed customer',
      customerNo: customer.customerNo ?? '',
      type: this.mapCustomerType(customer.customerType),
      segment: customer.segment ?? '-',
      status: customer.active ? 'Active' : 'Inactive',
      locations: 1,
      level: 0
    };
  }

  mapCustomerType(customerType: string): string {
    if (!customerType) {
      return 'Customer';
    }

    const normalized = customerType.toLowerCase();

    if (normalized.includes('smb') || normalized.includes('business')) {
      return 'Cluster';
    }

    if (normalized.includes('private')) {
      return 'Subcustomer';
    }

    return 'Customer';
  }

  visibleRows(): CustomerRow[] {
    const q = this.query.trim().toLowerCase();

    return this.rows.filter((row) => {
      const matchesQuery =
        !q ||
        row.name.toLowerCase().includes(q) ||
        row.customerNo.toLowerCase().includes(q);

      const matchesType =
        !this.typeFilter || row.type === this.typeFilter;

      const matchesStatus =
        !this.statusFilter || row.status === this.statusFilter;

      return matchesQuery && matchesType && matchesStatus;
    });
  }

  onApplyFilters(): void {
    this.loadCustomers();
    this.clearSelection();
  }

  onClearFilters(): void {
    this.query = '';
    this.customerNoFilter = '';
    this.typeFilter = '';
    this.statusFilter = '';
    this.saveSearchError = '';
    this.saveSearchSuccess = '';
    this.loadCustomers();
    this.clearSelection();
  }

  isSelected(id: string): boolean {
    return this.selectedIds.has(id);
  }

  toggleRow(id: string): void {
    if (this.selectedIds.has(id)) {
      this.selectedIds.delete(id);
    } else {
      this.selectedIds.add(id);
    }
  }

  get allSelected(): boolean {
    const ids = this.visibleRows().map((r) => r.id);
    return ids.length > 0 && ids.every((id) => this.selectedIds.has(id));
  }

  toggleAll(): void {
    const ids = this.visibleRows().map((r) => r.id);

    if (ids.length === 0) {
      return;
    }

    if (ids.every((id) => this.selectedIds.has(id))) {
      ids.forEach((id) => this.selectedIds.delete(id));
    } else {
      ids.forEach((id) => this.selectedIds.add(id));
    }
  }

  clearSelection(): void {
    this.selectedIds.clear();
    this.bulkOpen = false;
  }
  deleteSavedSearch(searchId: number) {

  const authenticationToken = this.authService.getAuthenticationToken();

  if (!authenticationToken) {
    return;
  }

  const confirmDelete = window.confirm("Delete this saved search?");

  if (!confirmDelete) {
    return;
  }

  this.customerApi.deleteSavedSearch(authenticationToken, searchId)
    .subscribe({
      next: () => {
        this.loadSavedSearches();
      },
      error: err => {
        console.error(err);
      }
    });
}
editSavedSearchName(search: SavedCustomerSearchDto) {

  const authenticationToken = this.authService.getAuthenticationToken();

  if (!authenticationToken) {
    return;
  }

  const newName = window.prompt("Edit search name", search.name);

  if (!newName || !newName.trim()) {
    return;
  }

  this.customerApi.updateSavedSearchName(
    authenticationToken,
    search.id,
    newName.trim()
  ).subscribe({
    next: () => {
      this.loadSavedSearches();
    },
    error: err => {
      console.error(err);
    }
  });
}
}