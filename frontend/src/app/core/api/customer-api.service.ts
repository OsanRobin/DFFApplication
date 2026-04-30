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

export interface CustomerSegmentDto {
  id: string;
  name: string | null;
  description: string | null;
}

export interface CustomerAttributeDto {
  name: string;
  value: string;
}

export interface CustomerAttributeRequest {
  name: string;
  value: string;
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
  attributes: CustomerAttributeDto[];
  subCustomers: CustomerDto[];
  parentClusterCustomers: CustomerDto[];
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
  flagFilter?: string;
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
  flagFilter?: string;
  overwrite: boolean;
}

export interface CustomerUserAttributeDto {
  name: string;
  value: string;
}

export interface CustomerUserDetailResponse {
  user: CustomerUserDto;
  attributes: CustomerUserAttributeDto[];
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

  getCustomerById(
    authenticationToken: string,
    domain: string,
    customerId: string
  ): Observable<CustomerDetailResponse> {
    const headers = new HttpHeaders({
      'authentication-token': authenticationToken
    });

    const params = new HttpParams().set('domain', domain);

    return this.http.get<CustomerDetailResponse>(`${this.baseUrl}/${customerId}`, {
      headers,
      params
    });
  }

  getCustomerUsers(
    authenticationToken: string,
    customerId: string
  ): Observable<CustomerUserListResponse> {
    const headers = new HttpHeaders({
      'authentication-token': authenticationToken
    });

    return this.http.get<CustomerUserListResponse>(`${this.baseUrl}/${customerId}/users`, {
      headers
    });
  }

  getCustomerUserDetail(
    authenticationToken: string,
    customerId: string,
    businessPartnerNo: string
  ): Observable<CustomerUserDetailResponse> {
    const headers = new HttpHeaders({
      'authentication-token': authenticationToken
    });

    return this.http.get<CustomerUserDetailResponse>(
      `${this.baseUrl}/${customerId}/users/${encodeURIComponent(businessPartnerNo)}`,
      { headers }
    );
  }

  addCustomerAttribute(
    authenticationToken: string,
    domain: string,
    customerId: string,
    request: CustomerAttributeRequest
  ): Observable<void> {
    const headers = new HttpHeaders({
      'authentication-token': authenticationToken
    });

    const params = new HttpParams().set('domain', domain);

    return this.http.post<void>(
      `${this.baseUrl}/${customerId}/attributes`,
      request,
      { headers, params }
    );
  }

  updateCustomerAttribute(
    authenticationToken: string,
    domain: string,
    customerId: string,
    attributeName: string,
    request: CustomerAttributeRequest
  ): Observable<void> {
    const headers = new HttpHeaders({
      'authentication-token': authenticationToken
    });

    const params = new HttpParams()
      .set('domain', domain)
      .set('attributeName', attributeName);

    return this.http.put<void>(
      `${this.baseUrl}/${customerId}/attributes`,
      request,
      { headers, params }
    );
  }

  deleteCustomerAttribute(
    authenticationToken: string,
    domain: string,
    customerId: string,
    attributeName: string
  ): Observable<void> {
    const headers = new HttpHeaders({
      'authentication-token': authenticationToken
    });

    const params = new HttpParams()
      .set('domain', domain)
      .set('attributeName', attributeName);

    return this.http.delete<void>(
      `${this.baseUrl}/${customerId}/attributes`,
      { headers, params }
    );
  }

  addCustomerUserAttribute(
    authenticationToken: string,
    customerId: string,
    businessPartnerNo: string,
    request: CustomerAttributeRequest
  ): Observable<void> {
    const headers = new HttpHeaders({
      'authentication-token': authenticationToken
    });

    return this.http.post<void>(
      `${this.baseUrl}/${customerId}/users/${encodeURIComponent(businessPartnerNo)}/attributes`,
      request,
      { headers }
    );
  }

  updateCustomerUserAttribute(
    authenticationToken: string,
    customerId: string,
    businessPartnerNo: string,
    attributeName: string,
    request: CustomerAttributeRequest
  ): Observable<void> {
    const headers = new HttpHeaders({
      'authentication-token': authenticationToken
    });

    const params = new HttpParams().set('attributeName', attributeName);

    return this.http.put<void>(
      `${this.baseUrl}/${customerId}/users/${encodeURIComponent(businessPartnerNo)}/attributes`,
      request,
      { headers, params }
    );
  }

  deleteCustomerUserAttribute(
    authenticationToken: string,
    customerId: string,
    businessPartnerNo: string,
    attributeName: string
  ): Observable<void> {
    const headers = new HttpHeaders({
      'authentication-token': authenticationToken
    });

    const params = new HttpParams().set('attributeName', attributeName);

    return this.http.delete<void>(
      `${this.baseUrl}/${customerId}/users/${encodeURIComponent(businessPartnerNo)}/attributes`,
      { headers, params }
    );
  }

  addCustomerToUserCustomerList(
    authenticationToken: string,
    customerId: string,
    businessPartnerNo: string,
    customerNo: string
  ): Observable<void> {
    const headers = new HttpHeaders({
      'authentication-token': authenticationToken
    });

    return this.http.post<void>(
      `${this.baseUrl}/${customerId}/users/${encodeURIComponent(businessPartnerNo)}/customer-list`,
      {
        name: 'CustomerList',
        value: customerNo
      },
      { headers }
    );
  }

  removeCustomerFromUserCustomerList(
    authenticationToken: string,
    customerId: string,
    businessPartnerNo: string,
    customerNo: string
  ): Observable<void> {
    const headers = new HttpHeaders({
      'authentication-token': authenticationToken
    });

    return this.http.delete<void>(
      `${this.baseUrl}/${customerId}/users/${encodeURIComponent(businessPartnerNo)}/customer-list/${encodeURIComponent(customerNo)}`,
      { headers }
    );
  }

  getSavedSearches(
    authenticationToken: string,
    domain: string
  ): Observable<SavedCustomerSearchListResponse> {
    const headers = new HttpHeaders({
      'authentication-token': authenticationToken
    });

    const params = new HttpParams().set('domain', domain);

    return this.http.get<SavedCustomerSearchListResponse>(this.savedSearchBaseUrl, {
      headers,
      params
    });
  }

  saveSearch(
    authenticationToken: string,
    request: SaveCustomerSearchRequest
  ): Observable<void> {
    const headers = new HttpHeaders({
      'authentication-token': authenticationToken
    });

    return this.http.post<void>(this.savedSearchBaseUrl, request, { headers });
  }

  deleteSavedSearch(authenticationToken: string, id: number): Observable<void> {
    const headers = new HttpHeaders({
      'authentication-token': authenticationToken
    });

    return this.http.delete<void>(`${this.savedSearchBaseUrl}/${id}`, { headers });
  }

  updateSavedSearchName(
    authenticationToken: string,
    id: number,
    name: string
  ): Observable<void> {
    const headers = new HttpHeaders({
      'authentication-token': authenticationToken
    });

    return this.http.put<void>(`${this.savedSearchBaseUrl}/${id}/name`, name, { headers });
  }
  addSubCustomerToCluster(
  authenticationToken: string,
  domain: string,
  clusterCustomerNo: string,
  subCustomerNo: string
): Observable<void> {
  const headers = new HttpHeaders({
    'authentication-token': authenticationToken
  });

  const params = new HttpParams().set('domain', domain);

  return this.http.post<void>(
    `${this.baseUrl}/${encodeURIComponent(clusterCustomerNo)}/relations/sub-customers/${encodeURIComponent(subCustomerNo)}`,
    null,
    { headers, params }
  );
}

removeSubCustomerFromCluster(
  authenticationToken: string,
  domain: string,
  clusterCustomerNo: string,
  subCustomerNo: string
): Observable<void> {
  const headers = new HttpHeaders({
    'authentication-token': authenticationToken
  });

  const params = new HttpParams().set('domain', domain);

  return this.http.delete<void>(
    `${this.baseUrl}/${encodeURIComponent(clusterCustomerNo)}/relations/sub-customers/${encodeURIComponent(subCustomerNo)}`,
    { headers, params }
  );
}

assignCustomerToCluster(
  authenticationToken: string,
  domain: string,
  customerNo: string,
  clusterCustomerNo: string
): Observable<void> {
  const headers = new HttpHeaders({
    'authentication-token': authenticationToken
  });

  const params = new HttpParams().set('domain', domain);

  return this.http.post<void>(
    `${this.baseUrl}/${encodeURIComponent(customerNo)}/relations/parent-clusters/${encodeURIComponent(clusterCustomerNo)}`,
    null,
    { headers, params }
  );
}

unassignCustomerFromCluster(
  authenticationToken: string,
  domain: string,
  customerNo: string,
  clusterCustomerNo: string
): Observable<void> {
  const headers = new HttpHeaders({
    'authentication-token': authenticationToken
  });

  const params = new HttpParams().set('domain', domain);

  return this.http.delete<void>(
    `${this.baseUrl}/${encodeURIComponent(customerNo)}/relations/parent-clusters/${encodeURIComponent(clusterCustomerNo)}`,
    { headers, params }
  );
}
getSegments(authenticationToken: string, domain: string): Observable<CustomerSegmentDto[]> {
  const headers = new HttpHeaders({
    'authentication-token': authenticationToken
  });

  const params = new HttpParams().set('domain', domain);

  return this.http.get<CustomerSegmentDto[]>(`${this.baseUrl}/segments`, {
    headers,
    params
  });
}

assignSegmentToCustomer(
  authenticationToken: string,
  domain: string,
  customerId: string,
  segmentId: string
): Observable<void> {
  const headers = new HttpHeaders({
    'authentication-token': authenticationToken
  });

  const params = new HttpParams().set('domain', domain);

  return this.http.post<void>(
    `${this.baseUrl}/${encodeURIComponent(customerId)}/segments`,
    { id: segmentId },
    { headers, params }
  );
}

removeSegmentFromCustomer(
  authenticationToken: string,
  domain: string,
  customerId: string,
  segmentId: string
): Observable<void> {
  const headers = new HttpHeaders({
    'authentication-token': authenticationToken
  });

  const params = new HttpParams().set('domain', domain);

  return this.http.delete<void>(
    `${this.baseUrl}/${encodeURIComponent(customerId)}/segments/${encodeURIComponent(segmentId)}`,
    { headers, params }
  );
}
}