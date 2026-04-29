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
import { BulkSelectionService, SelectedCustomerRow } from '../../core/api/bulk-selection.service';

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
  flags: string[];
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
  private bulkSelectionService = inject(BulkSelectionService);

  private domainName = 'DailyFreshFood-B1-Anonymous';

  query = '';
  customerNoFilter = '';
  typeFilter = '';
  statusFilter = '';
  segmentFilter = '';

  selectedIds = new Set<string>();
  selectedRows = new Map<string, CustomerRow>();
  expandedClusterIds = new Set<string>();

  loading = false;
  selectingAll = false;
  error = '';
  saveSearchError = '';
  saveSearchSuccess = '';

  rows: CustomerRow[] = [];
  savedSearches: SavedCustomerSearchDto[] = [];

  offset = 0;
  limit = 50;
  totalCount = 0;

  searchModalOpen = false;
  searchModalMode: 'create' | 'edit' = 'create';
  editingSavedSearch: SavedCustomerSearchDto | null = null;

  modalName = '';
  modalQuery = '';
  modalCustomerNo = '';
  modalType = '';
  modalStatus = '';
  modalSegment = '';

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
      this.offset,
      this.limit,
      this.customerNoFilter,
      this.query,
      this.typeFilter,
      this.statusFilter,
      this.segmentFilter
    ).subscribe({
      next: (response) => {
        this.rows = this.mapCustomersToRows(response.data ?? []);
        this.totalCount = response.count ?? 0;
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
    this.openSaveSearchModal();
  }

  openSaveSearchModal(): void {
    this.searchModalMode = 'create';
    this.editingSavedSearch = null;

    this.modalName = '';
    this.modalQuery = this.query;
    this.modalCustomerNo = this.customerNoFilter;
    this.modalType = this.typeFilter;
    this.modalStatus = this.statusFilter;
    this.modalSegment = this.segmentFilter;

    this.saveSearchError = '';
    this.saveSearchSuccess = '';
    this.searchModalOpen = true;
  }

  openEditSavedSearchModal(search: SavedCustomerSearchDto): void {
    this.searchModalMode = 'edit';
    this.editingSavedSearch = search;

    this.modalName = search.name ?? '';
    this.modalQuery = search.query ?? '';
    this.modalCustomerNo = search.customerNo ?? '';
    this.modalType = search.typeFilter ?? '';
    this.modalStatus = search.statusFilter ?? '';
    this.modalSegment = search.segmentFilter ?? '';

    this.saveSearchError = '';
    this.saveSearchSuccess = '';
    this.searchModalOpen = true;
  }

  closeSearchModal(): void {
    this.searchModalOpen = false;
    this.editingSavedSearch = null;
    this.saveSearchError = '';
  }

  submitSearchModal(): void {
    const authenticationToken = this.authService.getAuthenticationToken();

    if (!authenticationToken) {
      this.saveSearchError = 'No authentication token found. Please log in first.';
      return;
    }

    if (!this.modalName.trim()) {
      this.saveSearchError = 'Search name is required.';
      return;
    }

    this.saveSearchError = '';
    this.saveSearchSuccess = '';

    const request: SaveCustomerSearchRequest = {
      domainName: this.domainName,
      name: this.modalName.trim(),
      query: this.modalQuery.trim(),
      customerNo: this.modalCustomerNo.trim(),
      typeFilter: this.modalType,
      statusFilter: this.modalStatus,
      segmentFilter: this.modalSegment.trim(),
      overwrite: this.searchModalMode === 'edit'
    };

    const saveFilters = (): void => {
      this.customerApi.saveSearch(authenticationToken, request).subscribe({
        next: () => {
          this.saveSearchSuccess =
            this.searchModalMode === 'edit'
              ? 'Saved search updated successfully.'
              : 'Search saved successfully.';

          this.closeSearchModal();
          this.loadSavedSearches();
        },
        error: (err) => {
          const backendMessage = err?.error?.message ?? '';

          if (
            this.searchModalMode === 'create' &&
            backendMessage.toLowerCase().includes('already exists')
          ) {
            const overwrite = window.confirm(
              'A saved search with this name already exists. Overwrite it?'
            );

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
                this.closeSearchModal();
                this.loadSavedSearches();
              },
              error: (overwriteErr) => {
                console.error(overwriteErr);
                this.saveSearchError =
                  overwriteErr?.error?.message ?? 'Failed to overwrite saved search.';
              }
            });

            return;
          }

          console.error(err);
          this.saveSearchError = backendMessage || 'Failed to save search.';
        }
      });
    };

    if (
      this.searchModalMode === 'edit' &&
      this.editingSavedSearch &&
      this.editingSavedSearch.name !== request.name
    ) {
      this.customerApi.updateSavedSearchName(
        authenticationToken,
        this.editingSavedSearch.id,
        request.name
      ).subscribe({
        next: saveFilters,
        error: (err) => {
          console.error(err);
          this.saveSearchError =
            err?.error?.message ?? 'Failed to update saved search name.';
        }
      });

      return;
    }

    saveFilters();
  }

  applySavedSearch(savedSearch: SavedCustomerSearchDto): void {
    this.query = savedSearch.query ?? '';
    this.customerNoFilter = savedSearch.customerNo ?? '';
    this.typeFilter = savedSearch.typeFilter ?? '';
    this.statusFilter = savedSearch.statusFilter ?? '';
    this.segmentFilter = savedSearch.segmentFilter ?? '';

    this.onApplyFilters();
  }

  private mapCustomersToRows(customers: CustomerDto[]): CustomerRow[] {
    return customers.map(customer => {
      const row = this.mapCustomerToRow(customer);

      if (customer.parentCustomerNo) {
        return {
          ...row,
          level: 1,
          parentId: customer.parentCustomerNo
        };
      }

      return {
        ...row,
        level: 0
      };
    });
  }

  mapCustomerToRow(customer: CustomerDto): CustomerRow {
    const segment = customer.segment ?? '';
    const segments = this.parseSegments(segment);

    const hasCgSegment = segments.some(segmentValue =>
      segmentValue.toUpperCase().startsWith('CG')
    );

    const cfSegmentCount = segments.filter(segmentValue =>
      segmentValue.toUpperCase().startsWith('CF')
    ).length;

    const flags: string[] = [];

    if (!hasCgSegment) {
      flags.push('Missing CG segment');
    }

    if (cfSegmentCount > 1) {
      flags.push('Multiple CF segments');
    }

    return {
      id: customer.id,
      name: customer.displayName || customer.companyName || customer.customerNo || 'Unnamed customer',
      customerNo: customer.customerNo ?? '',
      type: customer.type ?? 'Customer',
      segment,
      status: customer.active ? 'Active' : 'Inactive',
      locations: customer.locations ?? 0,
      level: 0,
      flags
    };
  }

  private parseSegments(segment: string): string[] {
    if (!segment.trim()) {
      return [];
    }

    return segment
      .split(/[,\n;|]+/)
      .map(value => value.trim())
      .filter(Boolean);
  }

  visibleRows(): CustomerRow[] {
    return this.rows.filter(row => {
      if (row.level === 0) {
        return true;
      }

      return row.parentId ? this.expandedClusterIds.has(row.parentId) : true;
    });
  }

  toggleCluster(row: CustomerRow): void {
    if (row.type !== 'ClusterCustomer') {
      return;
    }

    if (this.expandedClusterIds.has(row.customerNo)) {
      this.expandedClusterIds.delete(row.customerNo);
    } else {
      this.expandedClusterIds.add(row.customerNo);
    }
  }

  isClusterExpanded(row: CustomerRow): boolean {
    return this.expandedClusterIds.has(row.customerNo);
  }

  onApplyFilters(): void {
    this.offset = 0;
    this.expandedClusterIds.clear();
    this.clearSelection();
    this.loadCustomers();
  }

  onClearFilters(): void {
    this.query = '';
    this.customerNoFilter = '';
    this.typeFilter = '';
    this.statusFilter = '';
    this.segmentFilter = '';
    this.saveSearchError = '';
    this.saveSearchSuccess = '';
    this.offset = 0;
    this.expandedClusterIds.clear();
    this.clearSelection();
    this.loadCustomers();
  }

  isSelected(id: string): boolean {
    return this.selectedIds.has(id);
  }

  toggleRow(row: CustomerRow): void {
    if (this.selectedIds.has(row.id)) {
      this.selectedIds.delete(row.id);
      this.selectedRows.delete(row.id);
    } else {
      this.selectedIds.add(row.id);
      this.selectedRows.set(row.id, row);
    }
  }

  get allVisibleSelected(): boolean {
    const rows = this.visibleRows();
    return rows.length > 0 && rows.every(row => this.selectedIds.has(row.id));
  }

  get someVisibleSelected(): boolean {
    return this.visibleRows().some(row => this.selectedIds.has(row.id));
  }

  toggleVisibleSelection(): void {
    const rows = this.visibleRows();

    if (rows.length === 0) {
      return;
    }

    const shouldUnselect = rows.every(row => this.selectedIds.has(row.id));

    if (shouldUnselect) {
      rows.forEach(row => {
        this.selectedIds.delete(row.id);
        this.selectedRows.delete(row.id);
      });

      return;
    }

    rows.forEach(row => {
      this.selectedIds.add(row.id);
      this.selectedRows.set(row.id, row);
    });
  }

  selectAll(): void {
    const authenticationToken = this.authService.getAuthenticationToken();

    if (!authenticationToken || this.totalCount === 0) {
      return;
    }

    this.selectingAll = true;
    this.error = '';

    this.customerApi.getCustomers(
      authenticationToken,
      this.domainName,
      0,
      this.totalCount,
      this.customerNoFilter,
      this.query,
      this.typeFilter,
      this.statusFilter,
      this.segmentFilter
    ).subscribe({
      next: (response) => {
        const allRows = this.mapCustomersToRows(response.data ?? []);

        allRows.forEach(row => {
          this.selectedIds.add(row.id);
          this.selectedRows.set(row.id, row);
        });

        this.selectingAll = false;
      },
      error: (err) => {
        console.error(err);
        this.error = 'Failed to select all customers.';
        this.selectingAll = false;
      }
    });
  }

  goToBulkActions(): void {
    const selectedCustomers: SelectedCustomerRow[] = Array.from(this.selectedRows.values())
      .map(row => ({
        id: row.id,
        name: row.name,
        customerNo: row.customerNo,
        type: row.type,
        segment: row.segment,
        status: row.status,
        locations: row.locations,
        level: row.level,
        parentId: row.parentId
      }));

    if (selectedCustomers.length === 0) {
      return;
    }

    this.bulkSelectionService.setSelectedCustomers(selectedCustomers);
    this.router.navigate(['/customers/bulk-actions']);
  }

  clearSelection(): void {
    this.selectedIds.clear();
    this.selectedRows.clear();
  }
  goBack(): void {
  this.router.navigate(['/dashboard']);
}

  deleteSavedSearch(searchId: number): void {
    const authenticationToken = this.authService.getAuthenticationToken();

    if (!authenticationToken) {
      return;
    }

    const confirmDelete = window.confirm('Delete this saved search?');

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

  previousPage(): void {
    if (this.offset >= this.limit) {
      this.offset -= this.limit;
      this.expandedClusterIds.clear();
      this.loadCustomers();
    }
  }

  nextPage(): void {
    if (this.offset + this.limit < this.totalCount) {
      this.offset += this.limit;
      this.expandedClusterIds.clear();
      this.loadCustomers();
    }
  }

  currentPage(): number {
    return Math.floor(this.offset / this.limit) + 1;
  }

  totalPages(): number {
    if (this.totalCount === 0) {
      return 1;
    }

    return Math.ceil(this.totalCount / this.limit);
  }

  pageStart(): number {
    if (this.totalCount === 0) {
      return 0;
    }

    return this.offset + 1;
  }

  pageEnd(): number {
    return Math.min(this.offset + this.limit, this.totalCount);
  }
}