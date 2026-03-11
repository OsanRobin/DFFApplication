import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CustomerDto {
  id: string;
  customerNo: string;
  customerType: string;
  displayName: string;
  companyName: string | null;
  email: string | null;
  segment: string | null;
  active: boolean;
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

@Injectable({
  providedIn: 'root'
})
export class CustomerApiService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8081/api/customers';

  getCustomers(
    authenticationToken: string,
    offset = 0,
    limit = 50,
    customerNo?: string
  ): Observable<CustomerListResponse> {
    let params = new HttpParams()
      .set('offset', offset)
      .set('limit', limit);

    if (customerNo?.trim()) {
      params = params.set('customerNo', customerNo.trim());
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
}