import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface CurrentUser {
  user: string;
  organization: string;
  roles: string[];
  managerRestricted: boolean;
}

export interface LoginPayload {
  username: string;
  password: string;
  organization: string;
}

export interface LoginResponse {
  user: string;
  organization: string;
  roles: string[];
  managerRestricted: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8081/api/auth';

  login(payload: LoginPayload): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, payload, { withCredentials: true }).pipe(
      tap(response => {
        localStorage.setItem('user', response.user);
        localStorage.setItem('organization', response.organization);
        localStorage.setItem('roles', JSON.stringify(response.roles ?? []));
        localStorage.setItem('managerRestricted', String(response.managerRestricted));
      })
    );
  }

  getCurrentUser(): Observable<CurrentUser> {
    return this.http.get<CurrentUser>(`${this.apiUrl}/me`, { withCredentials: true }).pipe(
      tap(user => {
        localStorage.setItem('user', user.user);
        localStorage.setItem('organization', user.organization);
        localStorage.setItem('roles', JSON.stringify(user.roles ?? []));
        localStorage.setItem('managerRestricted', String(user.managerRestricted));
      })
    );
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/logout`, {}, { withCredentials: true }).pipe(
      tap(() => {
        localStorage.removeItem('user');
        localStorage.removeItem('organization');
        localStorage.removeItem('authentication-token');
        localStorage.removeItem('roles');
        localStorage.removeItem('managerRestricted');
      })
    );
  }

  getUser(): string {
    return localStorage.getItem('user') ?? '';
  }

  getAuthenticationToken(): string {
    return localStorage.getItem('authentication-token') ?? '';
  }

  getRoles(): string[] {
    return JSON.parse(localStorage.getItem('roles') ?? '[]');
  }

  isManagerRestricted(): boolean {
    return localStorage.getItem('managerRestricted') === 'true';
  }

  isAuthenticated(): boolean {
    return localStorage.getItem('user') !== null;
  }
}