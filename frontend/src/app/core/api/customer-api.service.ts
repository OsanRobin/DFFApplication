import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CustomerDto {
  id: string;
  customerNo: string;
  customerType: string;
  type: string;
  displayName: string;
  companyName: string | null;
  email: string | null;
  segment: string | null;
  active: boolean;
  locations: number;
  customerList?: string | null;
  parentCustomerNo?: string | null;
}

export interface CustomerListResponse {
  offset: number;
  limit: number;
  count: number;
  data: CustomerDto[];
}

export interface CustomerAddressDto {
  id: string;
  addressName: string;
  firstName: string;
  lastName: string;
  companyName1: string;
  addressLine1: string;
  postalCode: string;
  country: string;
  countryCode: string;
  city: string;
  street: string;
  shipFromAddress: boolean;
  serviceToAddress: boolean;
  installToAddress: boolean;
  invoiceToAddress: boolean;
  shipToAddress: boolean;
  company: string;
}

export interface CustomerDetailResponse {
  customerNo: string;
  companyName: string;
  customerType: string;
  budgetPriceType: string;
  type: string;
  preferredInvoiceToAddress: CustomerAddressDto;
  preferredShipToAddress: CustomerAddressDto;
  segments: CustomerSegmentDto[];
}

export interface CustomerUserDto {
  name: string;
  login: string;
  firstName: string;
  lastName: string;
  active: boolean;
  businessPartnerNo: string;
  roleIds: string[];
  roleNames: string[];
  budgetPeriod: string;
  pendingOneTimeRequisitionsCount: number;
  pendingRecurringRequisitionsCount: number;
}

export interface CustomerUserListResponse {
  type: string;
  name: string;
  amount: number;
  elements: CustomerUserDto[];
  offset: number;
  limit: number;
  sortKeys: string[];
}

export interface SavedCustomerSearchDto {
  id: number;
  domainName: string;
  name: string;
  query: string;
  customerNo: string;
  typeFilter: string;
  statusFilter: string;
  segmentFilter: string;
}

export interface SavedCustomerSearchListResponse {
  count: number;
  data: SavedCustomerSearchDto[];
}

export interface SaveCustomerSearchRequest {
  domainName: string;
  name: string;
  query: string;
  customerNo: string;
  typeFilter: string;
  statusFilter: string;
  segmentFilter: string;
  overwrite: boolean;
}

export interface CustomerSegmentDto {
  id: string;
  name: string | null;
  description: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class CustomerApiService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8081/api/customers';
  private savedSearchBaseUrl = 'http://localhost:8081/api/customer-searches';

  getCustomers(
    authenticationToken: string,
    domain: string,
    offset = 0,
    limit = 1500,
    customerNo?: string,
    query?: string,
    type?: string,
    status?: string,
    segment?: string
  ): Observable<CustomerListResponse> {
    let params = new HttpParams()
      .set('domain', domain)
      .set('offset', offset)
      .set('limit', limit);

    if (customerNo?.trim()) {
      params = params.set('customerNo', customerNo.trim());
    }

    if (query?.trim()) {
      params = params.set('query', query.trim());
    }

    if (type?.trim()) {
      params = params.set('type', type.trim());
    }

    if (status?.trim()) {
      params = params.set('status', status.trim());
    }

    if (segment?.trim()) {
      params = params.set('segment', segment.trim());
    }

    const headers = new HttpHeaders({
      'authentication-token': authenticationToken
    });

    return this.http.get<CustomerListResponse>(this.baseUrl, {
      headers,
      params
    });
  }

  getCustomerById(authenticationToken: string, customerId: string): Observable<CustomerDetailResponse> {
    const headers = new HttpHeaders({
      'authentication-token': authenticationToken
    });

    return this.http.get<CustomerDetailResponse>(`${this.baseUrl}/${customerId}`, {
      headers
    });
  }

  getCustomerUsers(authenticationToken: string, customerId: string): Observable<CustomerUserListResponse> {
    const headers = new HttpHeaders({
      'authentication-token': authenticationToken
    });

    return this.http.get<CustomerUserListResponse>(`${this.baseUrl}/${customerId}/users`, {
      headers
    });
  }

  getSavedSearches(authenticationToken: string, domain: string): Observable<SavedCustomerSearchListResponse> {
    const headers = new HttpHeaders({
      'authentication-token': authenticationToken
    });

    const params = new HttpParams().set('domain', domain);

    return this.http.get<SavedCustomerSearchListResponse>(this.savedSearchBaseUrl, {
      headers,
      params
    });
  }

  saveSearch(authenticationToken: string, request: SaveCustomerSearchRequest): Observable<void> {
    const headers = new HttpHeaders({
      'authentication-token': authenticationToken
    });

    return this.http.post<void>(this.savedSearchBaseUrl, request, { headers });
  }

  deleteSavedSearch(authenticationToken: string, id: number) {
    const headers = new HttpHeaders({
      'authentication-token': authenticationToken
    });

    return this.http.delete(`${this.savedSearchBaseUrl}/${id}`, { headers });
  }

  updateSavedSearchName(authenticationToken: string, id: number, name: string) {
    const headers = new HttpHeaders({
      'authentication-token': authenticationToken
    });

    return this.http.put(`${this.savedSearchBaseUrl}/${id}/name`, name, { headers });
  }
}