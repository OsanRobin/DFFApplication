import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { BulkSelectionService, SelectedCustomerRow } from '../../core/api/bulk-selection.service';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../shell/header.component/header.component';

@Component({
  selector: 'app-bulk-actions.component',
  imports: [FormsModule, HeaderComponent],
  templateUrl: './bulk-actions.component.html',
  styleUrl: './bulk-actions.component.css',
})
export class BulkActionsComponent {

  private router = inject(Router);
  private bulkSelectionService = inject(BulkSelectionService);

  customers: SelectedCustomerRow[] = [];
  selectedAction = '';

  constructor() {
    this.customers = this.bulkSelectionService.getSelectedCustomers();

    if (this.customers.length === 0) {
      this.router.navigate(['/customers']);
    }
  }

  goBack(): void {
    this.router.navigate(['/customers']);
  }

  clearSelection(): void {
    this.bulkSelectionService.clear();
    this.router.navigate(['/customers']);
  }

  applyBulkAction(): void {
    if (!this.selectedAction) {
      alert('Please choose a bulk action first.');
      return;
    }

    switch (this.selectedAction) {
      case 'add-attribute':
        alert('Add Attribute action clicked');
        break;
      case 'update-attribute':
        alert('Update Attribute action clicked');
        break;
      case 'assign-segment':
        alert('Assign to Segment action clicked');
        break;
      default:
        break;
    }
  }
}
