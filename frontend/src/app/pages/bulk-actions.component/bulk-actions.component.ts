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

  availableAttributes: AttributeOption[] = [];

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
  }

  applyBulkAction(): void {
    this.resetMessages();

    if (!this.validateForm()) {
      return;
    }

    const request: BulkActionRequest = {
      customerIds: this.customers.map(customer => customer.id),
      action: this.selectedAction,
      attributeName: this.attributeName,
      attributeValue: this.attributeValue.trim()
    };

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
        this.loadingOptions = false;
      },
      error: () => {
        this.availableAttributes = [];
        this.errorMessage = 'Failed to load available attributes.';
        this.loadingOptions = false;
      }
    });
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

    if (this.selectedAction !== 'add-attribute' && this.selectedAction !== 'update-attribute') {
      this.errorMessage = 'Unsupported bulk action.';
      return false;
    }

    if (!this.attributeName) {
      this.errorMessage = 'Please select an attribute.';
      return false;
    }

    if (!this.attributeValue.trim()) {
      this.errorMessage = 'Attribute value is required.';
      return false;
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