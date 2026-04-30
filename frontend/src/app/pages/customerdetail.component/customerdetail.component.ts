import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { HeaderComponent } from '../../shell/header.component/header.component';
import {
  CustomerApiService,
  CustomerAttributeDto,
  CustomerDetailResponse,
  CustomerDto,
  CustomerSegmentDto,
  CustomerUserAttributeDto,
  CustomerUserDetailResponse,
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

  userDetailLoading = false;
  userDetailError = '';
  selectedUserDetail: CustomerUserDetailResponse | null = null;

  userCustomerListCustomers: CustomerDto[] = [];
  userCustomerListLoading = false;
  userCustomerListSaving = false;
  userCustomerListError = '';
  userCustomerNoToAdd = '';
  showUserCustomerAddForm = false;

  showDeleteUserCustomerConfirm = false;
  userCustomerNoToDelete = '';

  customerId = '';
  customer: CustomerDetailResponse | null = null;
  users: CustomerUserDto[] = [];

  editableAttributes: EditableAttribute[] = [];
  editingAttribute = false;
  editingAttributeId: string | null = null;
  attributeFormError = '';
  attributeSaving = false;
  relationSaving = false;
relationError = '';
showAddRelationForm = false;
relationCustomerNoToAdd = '';

showDeleteRelationConfirm = false;
relationCustomerNoToDelete = '';
relationDeleteMode: 'sub' | 'parent' | null = null;

  showDeleteAttributeConfirm = false;
  attributeNameToDelete = '';
  segmentsAll: CustomerSegmentDto[] = [];
segmentToAdd = '';
segmentSaving = false;
segmentError = '';

  attributeForm = {
    name: '',
    value: ''
  };

  editingUserAttribute = false;
  editingUserAttributeId: string | null = null;
  userAttributeSaving = false;
  userAttributeFormError = '';
  showDeleteUserAttributeConfirm = false;
  userAttributeNameToDelete = '';

  userAttributeForm = {
    name: '',
    value: ''
  };

  constructor() {
    this.route.paramMap.subscribe(params => {
      this.customerId = params.get('id') ?? '';

      const tabFromQuery = this.route.snapshot.queryParamMap.get('tab') as TabKey | null;
      this.activeTab = tabFromQuery ?? 'overview';

      this.customer = null;
      this.users = [];
      this.editableAttributes = [];
      this.usersLoaded = false;
      this.usersLoading = false;
      this.usersError = '';
      this.userDetailLoading = false;
      this.userDetailError = '';
      this.selectedUserDetail = null;
      this.userCustomerListCustomers = [];
      this.userCustomerListLoading = false;
      this.userCustomerListSaving = false;
      this.userCustomerListError = '';
      this.userCustomerNoToAdd = '';
      this.showUserCustomerAddForm = false;
      this.showDeleteUserCustomerConfirm = false;
      this.userCustomerNoToDelete = '';
      this.error = '';
      this.showDeleteAttributeConfirm = false;
      this.attributeNameToDelete = '';
      this.relationSaving = false;
this.relationError = '';
this.showAddRelationForm = false;
this.relationCustomerNoToAdd = '';
this.showDeleteRelationConfirm = false;
this.relationCustomerNoToDelete = '';
this.relationDeleteMode = null;
      this.cancelAttributeEdit();
      this.cancelUserAttributeEdit();

      this.loadCustomer();
      this.loadSegments();


      if (this.activeTab === 'users') {
        this.loadUsers();
      }
    });
  }

  goBack(): void {
    const returnCustomerId = this.route.snapshot.queryParamMap.get('returnCustomerId');

    if (returnCustomerId) {
      this.router.navigate(['/customers', returnCustomerId], {
        queryParams: {
          tab: 'users'
        }
      });
      return;
    }

    this.router.navigate(['/customers']);
  }

  setTab(tab: TabKey): void {
    this.activeTab = tab;

    if (tab !== 'users') {
      this.closeUserDetails();
    }

    if (tab === 'users' && !this.usersLoaded && !this.usersLoading) {
      this.loadUsers();
    }
  }

  displayText(value: unknown): string {
    if (value === null || value === undefined) {
      return '-';
    }

    const text = String(value).trim();

    if (!text || text.toLowerCase() === 'none' || text.toLowerCase() === 'null' || text.toLowerCase() === 'undefined') {
      return '-';
    }

    return text;
  }

  displayAttributeValue(attribute: { name: string; value: string | null | undefined }): string {
    const value = this.displayText(attribute.value);

    if (value === '-') {
      return '-';
    }

    if (attribute.name.toLowerCase().includes('days')) {
      return value
        .split(/[\t|,;\s]+/)
        .map(item => item.trim())
        .filter(Boolean)
        .join(', ');
    }

    return value;
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
  loadSegments(): void {
  const authenticationToken = this.authService.getAuthenticationToken();

  if (!authenticationToken) {
    this.segmentError = 'No authentication token found.';
    return;
  }

  this.customerApi.getSegments(authenticationToken, this.domainName).subscribe({
    next: response => {
      this.segmentsAll = response ?? [];
    },
    error: err => {
      console.error(err);
      this.segmentError = 'Failed to load segments.';
    }
  });
}

availableSegments(): CustomerSegmentDto[] {
  const assignedIds = new Set(
    (this.customer?.segments ?? []).map(segment => segment.id)
  );

  return this.segmentsAll.filter(segment => !assignedIds.has(segment.id));
}

assignSegment(): void {
  const authenticationToken = this.authService.getAuthenticationToken();

  if (!authenticationToken || !this.customerId || !this.segmentToAdd) {
    this.segmentError = 'Select a segment first.';
    return;
  }

  this.segmentSaving = true;
  this.segmentError = '';

  this.customerApi.assignSegmentToCustomer(
    authenticationToken,
    this.domainName,
    this.customerId,
    this.segmentToAdd
  ).subscribe({
    next: () => {
      this.segmentSaving = false;
      this.segmentToAdd = '';
      this.loadCustomer();
      this.loadSegments();
    },
    error: err => {
      console.error(err);
      this.segmentSaving = false;

      if (err.status === 401 || err.status === 403) {
        this.segmentError = 'Your session expired. Please log in again.';
        this.router.navigate(['/login']);
        return;
      }

      this.segmentError = 'Failed to assign segment.';
    }
  });
}

removeSegment(segmentId: string): void {
  const authenticationToken = this.authService.getAuthenticationToken();

  if (!authenticationToken || !this.customerId || !segmentId) {
    this.segmentError = 'Unable to remove segment.';
    return;
  }

  this.segmentSaving = true;
  this.segmentError = '';

  this.customerApi.removeSegmentFromCustomer(
    authenticationToken,
    this.domainName,
    this.customerId,
    segmentId
  ).subscribe({
    next: () => {
      this.segmentSaving = false;
      this.loadCustomer();
      this.loadSegments();
    },
    error: err => {
      console.error(err);
      this.segmentSaving = false;

      if (err.status === 401 || err.status === 403) {
        this.segmentError = 'Your session expired. Please log in again.';
        this.router.navigate(['/login']);
        return;
      }

      this.segmentError = 'Failed to remove segment.';
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

  viewUserDetails(user: CustomerUserDto): void {
    const authenticationToken = this.authService.getAuthenticationToken();

    if (!authenticationToken) {
      this.userDetailError = 'No authentication token found.';
      return;
    }

    if (!this.customerId || !user.businessPartnerNo) {
      this.userDetailError = 'Unable to load user details.';
      return;
    }

    this.selectedUserDetail = null;
    this.userCustomerListCustomers = [];
    this.userCustomerListLoading = false;
    this.userCustomerListError = '';
    this.userCustomerNoToAdd = '';
    this.showUserCustomerAddForm = false;
    this.showDeleteUserCustomerConfirm = false;
    this.userCustomerNoToDelete = '';
    this.cancelUserAttributeEdit();
    this.userDetailLoading = true;
    this.userDetailError = '';

    this.customerApi.getCustomerUserDetail(
      authenticationToken,
      this.customerId,
      user.businessPartnerNo
    ).subscribe({
      next: (response) => {
        const attributes = response.attributes ?? [];

        this.selectedUserDetail = {
          ...response,
          attributes: attributes.filter(attribute =>
            attribute.name.toLowerCase() !== 'customerlist'
          )
        };

        this.loadUserCustomerListCustomers(attributes);
        this.userDetailLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.userDetailLoading = false;

        if (err.status === 401 || err.status === 403) {
          this.userDetailError = 'Your session expired. Please log in again.';
          this.router.navigate(['/login']);
          return;
        }

        if (err.status === 404) {
          this.userDetailError = 'User details not found.';
          return;
        }

        this.userDetailError = 'Failed to load user details.';
      }
    });
  }

  private loadUserCustomerListCustomers(attributes: { name: string; value: string }[]): void {
    const customerListAttribute = attributes.find(attribute =>
      attribute.name.toLowerCase() === 'customerlist'
    );

    this.userCustomerListCustomers = [];

    if (!customerListAttribute?.value?.trim()) {
      return;
    }

    const customerNos = customerListAttribute.value
      .split(/[\t|,;\s]+/)
      .map(value => value.trim())
      .filter(Boolean);

    if (customerNos.length === 0) {
      return;
    }

    const authenticationToken = this.authService.getAuthenticationToken();

    if (!authenticationToken) {
      return;
    }

    this.userCustomerListLoading = true;

    forkJoin(
      customerNos.map(customerNo =>
        this.customerApi.getCustomers(
          authenticationToken,
          this.domainName,
          0,
          20,
          customerNo
        )
      )
    ).subscribe({
      next: responses => {
        const unique = new Map<string, CustomerDto>();

        responses
          .flatMap(response => response.data ?? [])
          .filter(customer => customerNos.includes(customer.customerNo))
          .forEach(customer => {
            unique.set(customer.customerNo, customer);
          });

        this.userCustomerListCustomers = Array.from(unique.values());
        this.userCustomerListLoading = false;
      },
      error: err => {
        console.error(err);
        this.userCustomerListLoading = false;
        this.userCustomerListError = 'Failed to load CustomerList customers.';
      }
    });
  }

  closeUserDetails(): void {
    this.selectedUserDetail = null;
    this.userDetailLoading = false;
    this.userDetailError = '';
    this.userCustomerListCustomers = [];
    this.userCustomerListLoading = false;
    this.userCustomerListSaving = false;
    this.userCustomerListError = '';
    this.userCustomerNoToAdd = '';
    this.showUserCustomerAddForm = false;
    this.showDeleteUserCustomerConfirm = false;
    this.userCustomerNoToDelete = '';
    this.cancelUserAttributeEdit();
  }

  userDetailTitle(): string {
    if (!this.selectedUserDetail?.user) {
      return 'User Details';
    }

    return this.displayText(
      this.selectedUserDetail.user.name
      || this.selectedUserDetail.user.login
      || this.selectedUserDetail.user.businessPartnerNo
      || 'User Details'
    );
  }

  buildEditableAttributes(attributes: CustomerAttributeDto[] = []): EditableAttribute[] {
    return attributes.map(attribute => ({
      id: attribute.name,
      name: attribute.name,
      value: attribute.value ?? ''
    }));
  }

  customerListCustomers(): CustomerDto[] {
    return this.customer?.subCustomers ?? [];
  }

  hasCustomerListCustomers(): boolean {
    return this.customerListCustomers().length > 0;
  }

  hasUserCustomerListCustomers(): boolean {
    return this.userCustomerListCustomers.length > 0;
  }

  userAttributes(): CustomerUserAttributeDto[] {
    return this.selectedUserDetail?.attributes ?? [];
  }

  hasUserAttributes(): boolean {
    return this.userAttributes().length > 0;
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

    if (this.attributeSaving) {
      return;
    }

    this.attributeNameToDelete = attributeName;
    this.showDeleteAttributeConfirm = true;
  }

  closeDeleteAttributeConfirm(): void {
    if (this.attributeSaving) {
      return;
    }

    this.showDeleteAttributeConfirm = false;
    this.attributeNameToDelete = '';
  }

  confirmDeleteAttribute(): void {
    const attributeName = this.attributeNameToDelete;

    if (!attributeName) {
      return;
    }

    const authenticationToken = this.authService.getAuthenticationToken();

    if (!authenticationToken) {
      this.attributeFormError = 'No authentication token found.';
      this.closeDeleteAttributeConfirm();
      return;
    }

    if (!this.customerId) {
      this.attributeFormError = 'No customer id found.';
      this.closeDeleteAttributeConfirm();
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
        this.showDeleteAttributeConfirm = false;

        if (this.editingAttributeId === attributeName) {
          this.cancelAttributeEdit();
        }

        this.attributeNameToDelete = '';
        this.loadCustomer();
      },
      error: (err) => {
        console.error(err);
        this.attributeSaving = false;
        this.showDeleteAttributeConfirm = false;
        this.attributeNameToDelete = '';

        if (err.status === 401 || err.status === 403) {
          this.attributeFormError = 'Your session expired. Please log in again.';
          this.router.navigate(['/login']);
          return;
        }

        this.attributeFormError = 'Failed to delete attribute.';
      }
    });
  }

  startAddUserAttribute(): void {
    this.editingUserAttribute = true;
    this.editingUserAttributeId = null;
    this.userAttributeFormError = '';
    this.userAttributeForm = {
      name: '',
      value: ''
    };
  }

  onEditUserAttributeClick(attributeName: string, event?: Event): void {
    event?.preventDefault();
    event?.stopPropagation();
    this.startEditUserAttribute(attributeName);
  }

  startEditUserAttribute(attributeName: string): void {
    const attribute = this.userAttributes().find(item => item.name === attributeName);

    if (!attribute) {
      this.userAttributeFormError = `Attribute not found: ${attributeName}`;
      return;
    }

    this.editingUserAttribute = true;
    this.editingUserAttributeId = attribute.name;
    this.userAttributeFormError = '';
    this.userAttributeForm = {
      name: attribute.name,
      value: attribute.value ?? ''
    };
  }

  cancelUserAttributeEdit(): void {
    this.editingUserAttribute = false;
    this.editingUserAttributeId = null;
    this.userAttributeSaving = false;
    this.userAttributeFormError = '';
    this.showDeleteUserAttributeConfirm = false;
    this.userAttributeNameToDelete = '';
    this.userAttributeForm = {
      name: '',
      value: ''
    };
  }

  saveUserAttribute(): void {
    const name = this.userAttributeForm.name.trim();
    const value = this.userAttributeForm.value.trim();

    if (!name) {
      this.userAttributeFormError = 'Attribute name is required.';
      return;
    }

    if (name.toLowerCase() === 'customerlist') {
      this.userAttributeFormError = 'CustomerList is managed in the User CustomerList Customers table.';
      return;
    }

    const authenticationToken = this.authService.getAuthenticationToken();
    const businessPartnerNo = this.selectedUserDetail?.user.businessPartnerNo;

    if (!authenticationToken || !this.customerId || !businessPartnerNo) {
      this.userAttributeFormError = 'Unable to save user attribute.';
      return;
    }

    const duplicate = this.userAttributes().some(attribute =>
      attribute.name.toLowerCase() === name.toLowerCase()
      && attribute.name !== this.editingUserAttributeId
    );

    if (duplicate) {
      this.userAttributeFormError = 'An attribute with this name already exists.';
      return;
    }

    this.userAttributeSaving = true;
    this.userAttributeFormError = '';

    if (this.editingUserAttributeId) {
      this.customerApi.updateCustomerUserAttribute(
        authenticationToken,
        this.customerId,
        businessPartnerNo,
        this.editingUserAttributeId,
        { name, value }
      ).subscribe({
        next: () => {
          const user = this.selectedUserDetail!.user;
          this.userAttributeSaving = false;
          this.cancelUserAttributeEdit();
          this.viewUserDetails(user);
        },
        error: err => {
          console.error(err);
          this.userAttributeSaving = false;

          if (err.status === 401 || err.status === 403) {
            this.userAttributeFormError = 'Your session expired. Please log in again.';
            this.router.navigate(['/login']);
            return;
          }

          this.userAttributeFormError = 'Failed to update user attribute.';
        }
      });

      return;
    }

    this.customerApi.addCustomerUserAttribute(
      authenticationToken,
      this.customerId,
      businessPartnerNo,
      { name, value }
    ).subscribe({
      next: () => {
        const user = this.selectedUserDetail!.user;
        this.userAttributeSaving = false;
        this.cancelUserAttributeEdit();
        this.viewUserDetails(user);
      },
      error: err => {
        console.error(err);
        this.userAttributeSaving = false;

        if (err.status === 401 || err.status === 403) {
          this.userAttributeFormError = 'Your session expired. Please log in again.';
          this.router.navigate(['/login']);
          return;
        }

        this.userAttributeFormError = 'Failed to add user attribute.';
      }
    });
  }

  removeUserAttribute(attributeName: string, event?: Event): void {
    event?.preventDefault();
    event?.stopPropagation();

    if (this.userAttributeSaving) {
      return;
    }

    this.userAttributeNameToDelete = attributeName;
    this.showDeleteUserAttributeConfirm = true;
  }

  closeDeleteUserAttributeConfirm(): void {
    if (this.userAttributeSaving) {
      return;
    }

    this.showDeleteUserAttributeConfirm = false;
    this.userAttributeNameToDelete = '';
  }

  confirmDeleteUserAttribute(): void {
    const attributeName = this.userAttributeNameToDelete;
    const authenticationToken = this.authService.getAuthenticationToken();
    const businessPartnerNo = this.selectedUserDetail?.user.businessPartnerNo;

    if (!authenticationToken || !this.customerId || !businessPartnerNo || !attributeName) {
      this.userAttributeFormError = 'Unable to delete user attribute.';
      this.closeDeleteUserAttributeConfirm();
      return;
    }

    this.userAttributeSaving = true;
    this.userAttributeFormError = '';

    this.customerApi.deleteCustomerUserAttribute(
      authenticationToken,
      this.customerId,
      businessPartnerNo,
      attributeName
    ).subscribe({
      next: () => {
        const user = this.selectedUserDetail!.user;
        this.userAttributeSaving = false;
        this.showDeleteUserAttributeConfirm = false;

        if (this.editingUserAttributeId === attributeName) {
          this.cancelUserAttributeEdit();
        }

        this.userAttributeNameToDelete = '';
        this.viewUserDetails(user);
      },
      error: err => {
        console.error(err);
        this.userAttributeSaving = false;
        this.showDeleteUserAttributeConfirm = false;
        this.userAttributeNameToDelete = '';

        if (err.status === 401 || err.status === 403) {
          this.userAttributeFormError = 'Your session expired. Please log in again.';
          this.router.navigate(['/login']);
          return;
        }

        this.userAttributeFormError = 'Failed to delete user attribute.';
      }
    });
  }

  addUserCustomerListCustomer(): void {
    const authenticationToken = this.authService.getAuthenticationToken();

    if (!authenticationToken || !this.selectedUserDetail?.user.businessPartnerNo) {
      this.userCustomerListError = 'Unable to add customer.';
      return;
    }

    const customerNo = this.userCustomerNoToAdd.trim();

    if (!customerNo) {
      this.userCustomerListError = 'Customer No is required.';
      return;
    }

    this.userCustomerListSaving = true;
    this.userCustomerListError = '';

    this.customerApi.addCustomerToUserCustomerList(
      authenticationToken,
      this.customerId,
      this.selectedUserDetail.user.businessPartnerNo,
      customerNo
    ).subscribe({
      next: () => {
        const user = this.selectedUserDetail!.user;
        this.userCustomerNoToAdd = '';
        this.showUserCustomerAddForm = false;
        this.userCustomerListSaving = false;
        this.viewUserDetails(user);
      },
      error: err => {
        console.error(err);
        this.userCustomerListSaving = false;
        this.userCustomerListError = 'Failed to add customer.';
      }
    });
  }

  openDeleteUserCustomerConfirm(customerNo: string): void {
    if (this.userCustomerListSaving) {
      return;
    }

    this.userCustomerNoToDelete = customerNo;
    this.showDeleteUserCustomerConfirm = true;
  }

  closeDeleteUserCustomerConfirm(): void {
    if (this.userCustomerListSaving) {
      return;
    }

    this.showDeleteUserCustomerConfirm = false;
    this.userCustomerNoToDelete = '';
  }

  confirmRemoveUserCustomerListCustomer(): void {
    const customerNo = this.userCustomerNoToDelete;
    const authenticationToken = this.authService.getAuthenticationToken();

    if (!authenticationToken || !this.selectedUserDetail?.user.businessPartnerNo || !customerNo) {
      this.userCustomerListError = 'Unable to remove customer.';
      return;
    }

    this.userCustomerListSaving = true;
    this.userCustomerListError = '';

    this.customerApi.removeCustomerFromUserCustomerList(
      authenticationToken,
      this.customerId,
      this.selectedUserDetail.user.businessPartnerNo,
      customerNo
    ).subscribe({
      next: () => {
        const user = this.selectedUserDetail!.user;
        this.userCustomerListSaving = false;
        this.showDeleteUserCustomerConfirm = false;
        this.userCustomerNoToDelete = '';
        this.viewUserDetails(user);
      },
      error: err => {
        console.error(err);
        this.userCustomerListSaving = false;
        this.showDeleteUserCustomerConfirm = false;
        this.userCustomerNoToDelete = '';
        this.userCustomerListError = 'Failed to remove customer.';
      }
    });
  }

  customerTitle(): string {
    return this.displayText(this.customer?.companyName || this.customer?.customerNo || this.customerId || 'Customer Detail');
  }

  customerNumber(): string {
    return this.displayText(this.customer?.customerNo || this.customerId);
  }

  customerTypeLabel(): string {
    return this.displayText(this.customer?.customerType);
  }

  objectTypeLabel(): string {
    return this.displayText(this.customer?.type);
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
    ].map(line => this.displayText(line)).filter(line => line !== '-');
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
    ].map(line => this.displayText(line)).filter(line => line !== '-');
  }

  displayRoles(user: CustomerUserDto): string {
    const roles = (user.roleNames ?? [])
      .map(role => this.displayText(role))
      .filter(role => role !== '-');

    if (roles.length > 0) {
      return roles.join(', ');
    }

    return '-';
  }

  displayStatus(user: CustomerUserDto): string {
    return user.active ? 'Active' : 'Inactive';
  }

  relationName(customer: CustomerDto): string {
    return this.displayText(customer.displayName || customer.companyName || customer.customerNo);
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
  isClusterCustomer(): boolean {
  return this.customer?.type?.toLowerCase() === 'clustercustomer';
}

relationAddLabel(): string {
  return this.isClusterCustomer() ? 'Add Customer' : 'Assign ClusterCustomer';
}

relationInputPlaceholder(): string {
  return this.isClusterCustomer() ? 'Customer No' : 'ClusterCustomer No';
}

openAddRelationForm(): void {
  this.showAddRelationForm = true;
  this.relationCustomerNoToAdd = '';
  this.relationError = '';
}

closeAddRelationForm(): void {
  if (this.relationSaving) {
    return;
  }

  this.showAddRelationForm = false;
  this.relationCustomerNoToAdd = '';
  this.relationError = '';
}

saveRelation(): void {
  const authenticationToken = this.authService.getAuthenticationToken();
  const value = this.relationCustomerNoToAdd.trim();

  if (!authenticationToken || !this.customerId || !value) {
    this.relationError = this.isClusterCustomer()
      ? 'Customer No is required.'
      : 'ClusterCustomer No is required.';
    return;
  }

  this.relationSaving = true;
  this.relationError = '';

  const request = this.isClusterCustomer()
    ? this.customerApi.addSubCustomerToCluster(authenticationToken, this.domainName, this.customerId, value)
    : this.customerApi.assignCustomerToCluster(authenticationToken, this.domainName, this.customerId, value);

  request.subscribe({
    next: () => {
      this.relationSaving = false;
      this.showAddRelationForm = false;
      this.relationCustomerNoToAdd = '';
      this.loadCustomer();
    },
    error: err => {
      console.error(err);
      this.relationSaving = false;

      if (err.status === 401 || err.status === 403) {
        this.relationError = 'Your session expired. Please log in again.';
        this.router.navigate(['/login']);
        return;
      }

      this.relationError = 'Failed to save relation.';
    }
  });
}

openDeleteRelationConfirm(customerNo: string, mode: 'sub' | 'parent', event?: Event): void {
  event?.preventDefault();
  event?.stopPropagation();

  if (this.relationSaving) {
    return;
  }

  this.relationCustomerNoToDelete = customerNo;
  this.relationDeleteMode = mode;
  this.showDeleteRelationConfirm = true;
}
closeDeleteRelationConfirm(): void {
  if (this.relationSaving) {
    return;
  }

  this.showDeleteRelationConfirm = false;
  this.relationCustomerNoToDelete = '';
  this.relationDeleteMode = null;
}

confirmDeleteRelation(): void {
  const authenticationToken = this.authService.getAuthenticationToken();

  if (!authenticationToken || !this.customerId || !this.relationCustomerNoToDelete || !this.relationDeleteMode) {
    this.relationError = 'Unable to remove relation.';
    this.closeDeleteRelationConfirm();
    return;
  }

  this.relationSaving = true;
  this.relationError = '';

  const request = this.relationDeleteMode === 'sub'
    ? this.customerApi.removeSubCustomerFromCluster(
        authenticationToken,
        this.domainName,
        this.customerId,
        this.relationCustomerNoToDelete
      )
    : this.customerApi.unassignCustomerFromCluster(
        authenticationToken,
        this.domainName,
        this.customerId,
        this.relationCustomerNoToDelete
      );

  request.subscribe({
    next: () => {
      this.relationSaving = false;
      this.closeDeleteRelationConfirm();
      this.loadCustomer();
    },
    error: err => {
      console.error(err);
      this.relationSaving = false;
      this.showDeleteRelationConfirm = false;
      this.relationCustomerNoToDelete = '';
      this.relationDeleteMode = null;

      if (err.status === 401 || err.status === 403) {
        this.relationError = 'Your session expired. Please log in again.';
        this.router.navigate(['/login']);
        return;
      }

      this.relationError = 'Failed to remove relation.';
    }
  });
}
}