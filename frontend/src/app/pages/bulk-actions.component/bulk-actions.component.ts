import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HeaderComponent } from '../../shell/header.component/header.component';
import {
  AttributeOption,
  BulkActionRequest,
  BulkActionResponse,
  BulkSelectionService,
  SegmentOption,
  SelectedCustomerRow
} from '../../core/api/bulk-selection.service';

@Component({
  selector: 'app-bulk-actions',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, HeaderComponent],
  templateUrl: './bulk-actions.component.html',
  styleUrl: './bulk-actions.component.css',
})
export class BulkActionsComponent {
  private router = inject(Router);
  private bulkSelectionService = inject(BulkSelectionService);

  customers: SelectedCustomerRow[] = [];
  selectedAction = '';

  attributeName = '';
  attributeValue = '';
  segmentId = '';

  availableAttributes: AttributeOption[] = [];
  availableSegments: SegmentOption[] = [];

  loading = false;
  loadingOptions = false;
  errorMessage = '';
  successMessage = '';

  constructor() {
    this.customers = this.bulkSelectionService.getSelectedCustomers();

    if (this.customers.length === 0) {
      this.router.navigate(['/customers']);
      return;
    }

    this.loadOptions();
  }

  goBack(): void {
    this.router.navigate(['/customers']);
  }

  clearSelection(): void {
    this.bulkSelectionService.clear();
    this.customers = [];
    this.router.navigate(['/customers']);
  }

  onActionChange(): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.attributeName = '';
    this.attributeValue = '';
    this.segmentId = '';
  }

  applyBulkAction(): void {
    this.resetMessages();

    if (!this.validateForm()) {
      return;
    }

    const request: BulkActionRequest = {
      customerIds: this.customers.map(customer => customer.id),
      action: this.selectedAction
    };

    if (this.selectedAction === 'add-attribute' || this.selectedAction === 'update-attribute') {
      request.attributeName = this.attributeName;
      request.attributeValue = this.attributeValue.trim();
    }

    if (this.selectedAction === 'assign-segment') {
      request.segmentId = this.segmentId;
    }

    this.loading = true;

    this.bulkSelectionService.executeBulkAction(request).subscribe({
      next: (response: BulkActionResponse) => {
        this.loading = false;

        if (response.success) {
          this.successMessage =
            response.message || `${response.processedCount} customers updated successfully.`;

          this.selectedAction = '';
          this.attributeName = '';
          this.attributeValue = '';
          this.segmentId = '';
        } else {
          this.errorMessage = this.buildErrorMessage(response);
        }
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage =
          error?.error?.message ||
          error?.error?.error ||
          'Bulk action failed.';
      }
    });
  }

  private loadOptions(): void {
    this.loadingOptions = true;
    this.errorMessage = '';

    this.bulkSelectionService.getAvailableAttributes().subscribe({
      next: (attributes) => {
        this.availableAttributes = attributes;
        this.checkIfOptionsFinishedLoading();
      },
      error: () => {
        this.availableAttributes = [];
        this.errorMessage = 'Failed to load available attributes.';
        this.checkIfOptionsFinishedLoading();
      }
    });

    this.bulkSelectionService.getAvailableSegments().subscribe({
      next: (segments) => {
        this.availableSegments = segments;
        this.checkIfOptionsFinishedLoading();
      },
      error: () => {
        this.availableSegments = [];
        this.errorMessage = this.errorMessage || 'Failed to load available segments.';
        this.checkIfOptionsFinishedLoading();
      }
    });
  }

  private pendingLoads = 2;

  private checkIfOptionsFinishedLoading(): void {
    this.pendingLoads--;

    if (this.pendingLoads <= 0) {
      this.loadingOptions = false;
      this.pendingLoads = 2;
    }
  }

  private validateForm(): boolean {
    if (this.customers.length === 0) {
      this.errorMessage = 'No customers selected.';
      return false;
    }

    if (!this.selectedAction) {
      this.errorMessage = 'Please choose a bulk action first.';
      return false;
    }

    if (this.selectedAction === 'add-attribute' || this.selectedAction === 'update-attribute') {
      if (!this.attributeName) {
        this.errorMessage = 'Please select an attribute.';
        return false;
      }

      if (!this.attributeValue.trim()) {
        this.errorMessage = 'Attribute value is required.';
        return false;
      }
    }

    if (this.selectedAction === 'assign-segment') {
      if (!this.segmentId) {
        this.errorMessage = 'Please select a segment.';
        return false;
      }
    }

    return true;
  }

  private buildErrorMessage(response: BulkActionResponse): string {
    if (response.errors && response.errors.length > 0) {
      return `${response.message}: ${response.errors.join(' | ')}`;
    }

    return response.message || 'Bulk action failed.';
  }

  private resetMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }
}