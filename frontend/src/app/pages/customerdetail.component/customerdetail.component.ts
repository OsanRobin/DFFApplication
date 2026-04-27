import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HeaderComponent } from '../../shell/header.component/header.component';
import {
  CustomerApiService,
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
          subCustomers: response.subCustomers ?? [],
          parentClusterCustomers: response.parentClusterCustomers ?? []
        };

        this.editableAttributes = this.buildEditableAttributes(this.customer);
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

  buildEditableAttributes(customer: CustomerDetailResponse): EditableAttribute[] {
    return [
      {
        id: 'budgetPriceType',
        name: 'Budget Price Type',
        value: customer.budgetPriceType || ''
      },
      {
        id: 'customerType',
        name: 'Customer Type ID',
        value: customer.customerType || ''
      },
      {
        id: 'type',
        name: 'Customer Type',
        value: customer.type || ''
      },
      {
        id: 'companyName',
        name: 'Company Name',
        value: customer.companyName || ''
      }
    ];
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

  startEditAttribute(attributeId: string): void {
    const attribute = this.editableAttributes.find(item => item.id === attributeId);

    if (!attribute) {
      return;
    }

    this.editingAttribute = true;
    this.editingAttributeId = attribute.id;
    this.attributeFormError = '';
    this.attributeForm = {
      name: attribute.name,
      value: attribute.value
    };
  }

  cancelAttributeEdit(): void {
    this.editingAttribute = false;
    this.editingAttributeId = null;
    this.attributeFormError = '';
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

    const duplicate = this.editableAttributes.some(attribute => {
      return attribute.name.toLowerCase() === name.toLowerCase()
        && attribute.id !== this.editingAttributeId;
    });

    if (duplicate) {
      this.attributeFormError = 'An attribute with this name already exists.';
      return;
    }

    if (this.editingAttributeId) {
      this.editableAttributes = this.editableAttributes.map(attribute => {
        if (attribute.id !== this.editingAttributeId) {
          return attribute;
        }

        return {
          ...attribute,
          name,
          value
        };
      });

      this.updateCustomerPreviewAttribute(this.editingAttributeId, value);
      this.cancelAttributeEdit();
      return;
    }

    this.editableAttributes = [
      ...this.editableAttributes,
      {
        id: `custom-${Date.now()}`,
        name,
        value
      }
    ];

    this.cancelAttributeEdit();
  }

  removeAttribute(attributeId: string): void {
    this.editableAttributes = this.editableAttributes.filter(attribute => attribute.id !== attributeId);

    if (this.editingAttributeId === attributeId) {
      this.cancelAttributeEdit();
    }
  }

  updateCustomerPreviewAttribute(attributeId: string, value: string): void {
    if (!this.customer) {
      return;
    }

    if (attributeId === 'budgetPriceType') {
      this.customer = { ...this.customer, budgetPriceType: value };
    }

    if (attributeId === 'customerType') {
      this.customer = { ...this.customer, customerType: value };
    }

    if (attributeId === 'type') {
      this.customer = { ...this.customer, type: value };
    }

    if (attributeId === 'companyName') {
      this.customer = { ...this.customer, companyName: value };
    }
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