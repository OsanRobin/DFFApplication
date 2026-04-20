import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HeaderComponent } from '../../shell/header.component/header.component';
import {
  CustomerApiService,
  CustomerDetailResponse,
  CustomerUserDto
} from '../../core/api/customer-api.service';
import { AuthService } from '../../core/auth/auth.service';

type TabKey = 'overview' | 'attributes' | 'users' | 'segments';

@Component({
  selector: 'app-customerdetail.component',
  imports: [CommonModule, HeaderComponent],
  templateUrl: './customerdetail.component.html',
  styleUrl: './customerdetail.component.css',
})
export class CustomerdetailComponent {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private customerApi = inject(CustomerApiService);
  private authService = inject(AuthService);

  activeTab: TabKey = 'overview';

  loading = false;
  error = '';

  usersLoading = false;
  usersError = '';
  usersLoaded = false;

  customerId = '';
  customer: CustomerDetailResponse | null = null;
  users: CustomerUserDto[] = [];

  constructor() {
    this.customerId = this.route.snapshot.paramMap.get('id') ?? '';
    this.loadCustomer();
  }

  goBack(): void {
    history.back();
  }

  setTab(tab: TabKey): void {
    this.activeTab = tab;

    if (tab === 'users' && !this.usersLoaded && !this.usersLoading) {
      this.loadUsers();
    }
  }

  loadCustomer(): void {
    const authenticationToken = this.authService.getAuthenticationToken();

    if (!authenticationToken) {
      this.error = 'No authentication token found. Please log in first.';
      return;
    }

    if (!this.customerId) {
      this.error = 'No customer id found in route.';
      return;
    }

    this.loading = true;
    this.error = '';

    this.customerApi.getCustomerById(authenticationToken, this.customerId).subscribe({
      next: (response) => {
        this.customer = response;
        console.log('Customer detail response:', response);
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

        if (err.status === 404) {
          this.error = 'Customer detail is not available yet for this customer.';
          this.loading = false;
          return;
        }

        this.error = 'Failed to load customer details.';
        this.loading = false;
      }
    });
  }

  loadUsers(): void {
    const authenticationToken = this.authService.getAuthenticationToken();

    if (!authenticationToken || !this.customerId) {
      this.usersError = 'Unable to load users.';
      return;
    }

    this.usersLoading = true;
    this.usersError = '';

    this.customerApi.getCustomerUsers(authenticationToken, this.customerId).subscribe({
      next: (response) => {
        this.users = response.elements ?? [];
        this.usersLoaded = true;
        this.usersLoading = false;
      },
      error: (err) => {
        console.error(err);

        if (err.status === 401 || err.status === 403) {
          this.usersError = 'Your session expired. Please log in again.';
          this.usersLoading = false;
          this.router.navigate(['/login']);
          return;
        }

        this.usersError = 'Failed to load users.';
        this.usersLoading = false;
      }
    });
  }

  customerTitle(): string {
    if (this.customer?.companyName) {
      return this.customer.companyName;
    }

    if (this.customer?.customerNo) {
      return this.customer.customerNo;
    }

    return this.customerId || 'Customer Detail';
  }

  customerNumber(): string {
    return this.customer?.customerNo || this.customerId || '-';
  }

  customerTypeLabel(): string {
    if (!this.customer?.customerType) {
      return '-';
    }

    return this.customer.customerType;
  }

  objectTypeLabel(): string {
    if (!this.customer?.type || !this.customer.type.trim()) {
      return '-';
    }

    return this.customer.type;
  }

  statusLabel(): string {
    return 'Active';
  }

  invoiceAddressLines(): string[] {
    const address = this.customer?.preferredInvoiceToAddress;
    if (!address) {
      return [];
    }

    return [
      address.company || address.companyName1,
      address.addressLine1 || address.street,
      `${address.postalCode ?? ''} ${address.city ?? ''}`.trim(),
      address.country
    ].filter((line): line is string => !!line && line.trim().length > 0);
  }

  shippingAddressLines(): string[] {
    const address = this.customer?.preferredShipToAddress;
    if (!address) {
      return [];
    }

    return [
      address.company || address.companyName1,
      address.addressLine1 || address.street,
      `${address.postalCode ?? ''} ${address.city ?? ''}`.trim(),
      address.country
    ].filter((line): line is string => !!line && line.trim().length > 0);
  }

  displayRoles(user: CustomerUserDto): string {
    const roles = (user.roleNames ?? []).filter((role) => !!role && role.trim().length > 0);

    if (roles.length > 0) {
      return roles.join(', ');
    }

    return '-';
  }

  displayStatus(user: CustomerUserDto): string {
    return user.active ? 'Active' : 'Inactive';
  }
}