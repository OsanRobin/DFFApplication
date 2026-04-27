import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HeaderComponent } from '../../shell/header.component/header.component';
import {
  CustomerApiService,
  CustomerAttributeDto,
  CustomerDetailResponse,
  CustomerDto,
  CustomerUserDto
} from '../../core/api/customer-api.service';
import { AuthService } from '../../core/auth/auth.service';

type TabKey = 'overview' | 'attributes' | 'users' | 'segments' | 'relations';

type EditableAttribute = {
  id: string;
  name: string;
  value: string;
};

@Component({
  selector: 'app-customerdetail.component',
  imports: [CommonModule, FormsModule, RouterModule, HeaderComponent],
  templateUrl: './customerdetail.component.html',
  styleUrl: './customerdetail.component.css',
})
export class CustomerdetailComponent {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private customerApi = inject(CustomerApiService);
  private authService = inject(AuthService);

  private domainName = 'DailyFreshFood-B1-Anonymous';

  activeTab: TabKey = 'overview';

  loading = false;
  error = '';

  usersLoading = false;
  usersError = '';
  usersLoaded = false;

  customerId = '';
  customer: CustomerDetailResponse | null = null;
  users: CustomerUserDto[] = [];

  editableAttributes: EditableAttribute[] = [];
  editingAttribute = false;
  editingAttributeId: string | null = null;
  attributeFormError = '';
  attributeSaving = false;

  attributeForm = {
    name: '',
    value: ''
  };

  constructor() {
    this.route.paramMap.subscribe(params => {
      this.customerId = params.get('id') ?? '';

      this.customer = null;
      this.users = [];
      this.editableAttributes = [];
      this.usersLoaded = false;
      this.usersLoading = false;
      this.usersError = '';
      this.error = '';
      this.activeTab = 'overview';
      this.cancelAttributeEdit();

      this.loadCustomer();
    });
  }

  goBack(): void {
    this.router.navigate(['/customers']);
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

    this.customerApi.getCustomerById(authenticationToken, this.domainName, this.customerId).subscribe({
      next: (response) => {
        this.customer = {
          ...response,
          segments: response.segments ?? [],
          attributes: response.attributes ?? [],
          subCustomers: response.subCustomers ?? [],
          parentClusterCustomers: response.parentClusterCustomers ?? []
        };

        this.editableAttributes = this.buildEditableAttributes(this.customer.attributes);
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

  buildEditableAttributes(attributes: CustomerAttributeDto[] = []): EditableAttribute[] {
    return attributes.map(attribute => ({
      id: attribute.name,
      name: attribute.name,
      value: attribute.value ?? ''
    }));
  }

  startAddAttribute(): void {
    this.editingAttribute = true;
    this.editingAttributeId = null;
    this.attributeFormError = '';
    this.attributeForm = {
      name: '',
      value: ''
    };
  }

  onEditAttributeClick(attributeName: string, event?: Event): void {
    event?.preventDefault();
    event?.stopPropagation();
    this.startEditAttribute(attributeName);
  }

  startEditAttribute(attributeName: string): void {
    const attribute = this.editableAttributes.find(item => item.name === attributeName);

    if (!attribute) {
      this.attributeFormError = `Attribute not found: ${attributeName}`;
      return;
    }

    this.editingAttribute = true;
    this.editingAttributeId = attribute.name;
    this.attributeFormError = '';
    this.attributeForm = {
      name: attribute.name,
      value: attribute.value ?? ''
    };
  }

  cancelAttributeEdit(): void {
    this.editingAttribute = false;
    this.editingAttributeId = null;
    this.attributeFormError = '';
    this.attributeSaving = false;
    this.attributeForm = {
      name: '',
      value: ''
    };
  }

  saveAttribute(): void {
    const name = this.attributeForm.name.trim();
    const value = this.attributeForm.value.trim();

    if (!name) {
      this.attributeFormError = 'Attribute name is required.';
      return;
    }

    const authenticationToken = this.authService.getAuthenticationToken();

    if (!authenticationToken) {
      this.attributeFormError = 'No authentication token found.';
      return;
    }

    if (!this.customerId) {
      this.attributeFormError = 'No customer id found.';
      return;
    }

    const duplicate = this.editableAttributes.some(attribute => {
      return attribute.name.toLowerCase() === name.toLowerCase()
        && attribute.name !== this.editingAttributeId;
    });

    if (duplicate) {
      this.attributeFormError = 'An attribute with this name already exists.';
      return;
    }

    this.attributeSaving = true;
    this.attributeFormError = '';

    if (this.editingAttributeId) {
      this.customerApi.updateCustomerAttribute(
        authenticationToken,
        this.domainName,
        this.customerId,
        this.editingAttributeId,
        { name, value }
      ).subscribe({
        next: () => {
          this.attributeSaving = false;
          this.cancelAttributeEdit();
          this.loadCustomer();
        },
        error: (err) => {
          console.error(err);
          this.attributeSaving = false;

          if (err.status === 401 || err.status === 403) {
            this.attributeFormError = 'Your session expired. Please log in again.';
            this.router.navigate(['/login']);
            return;
          }

          this.attributeFormError = 'Failed to update attribute.';
        }
      });

      return;
    }

    this.customerApi.addCustomerAttribute(
      authenticationToken,
      this.domainName,
      this.customerId,
      { name, value }
    ).subscribe({
      next: () => {
        this.attributeSaving = false;
        this.cancelAttributeEdit();
        this.loadCustomer();
      },
      error: (err) => {
        console.error(err);
        this.attributeSaving = false;

        if (err.status === 401 || err.status === 403) {
          this.attributeFormError = 'Your session expired. Please log in again.';
          this.router.navigate(['/login']);
          return;
        }

        this.attributeFormError = 'Failed to add attribute.';
      }
    });
  }

  removeAttribute(attributeName: string, event?: Event): void {
  event?.preventDefault();
  event?.stopPropagation();

  const authenticationToken = this.authService.getAuthenticationToken();

  if (!authenticationToken) {
    this.attributeFormError = 'No authentication token found.';
    return;
  }

  if (!this.customerId) {
    this.attributeFormError = 'No customer id found.';
    return;
  }

  this.attributeSaving = true;
  this.attributeFormError = '';

  this.customerApi.deleteCustomerAttribute(
    authenticationToken,
    this.domainName,
    this.customerId,
    attributeName
  ).subscribe({
    next: () => {
      this.attributeSaving = false;

      if (this.editingAttributeId === attributeName) {
        this.cancelAttributeEdit();
      }

      this.loadCustomer();
    },
    error: (err) => {
      console.error(err);
      this.attributeSaving = false;

      if (err.status === 401 || err.status === 403) {
        this.attributeFormError = 'Your session expired. Please log in again.';
        this.router.navigate(['/login']);
        return;
      }

      this.attributeFormError = 'Failed to delete attribute.';
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
    return this.customer?.customerType || '-';
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

  relationName(customer: CustomerDto): string {
    return customer.displayName || customer.companyName || customer.customerNo || '-';
  }

  hasParentClusters(): boolean {
    return (this.customer?.parentClusterCustomers?.length ?? 0) > 0;
  }

  hasSubCustomers(): boolean {
    return (this.customer?.subCustomers?.length ?? 0) > 0;
  }

  hasRelations(): boolean {
    return this.hasParentClusters() || this.hasSubCustomers();
  }
}