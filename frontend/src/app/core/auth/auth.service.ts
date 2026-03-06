import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface CurrentUser {
  user: string;
  organization: string;
}

export interface LoginPayload {
  username: string;
  password: string;
  organization: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8081/api/auth';

  login(payload: LoginPayload): Observable<CurrentUser> {
    return this.http.post<CurrentUser>(`${this.apiUrl}/login`, payload, { withCredentials: true }).pipe(
      tap(user => {
        localStorage.setItem('user', user.user);
        localStorage.setItem('organization', user.organization);
      })
    );
  }

  getCurrentUser(): Observable<CurrentUser> {
    return this.http.get<CurrentUser>(`${this.apiUrl}/me`, { withCredentials: true }).pipe(
      tap(user => {
        localStorage.setItem('user', user.user);
        localStorage.setItem('organization', user.organization);
      })
    );
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/logout`, {}, { withCredentials: true }).pipe(
      tap(() => localStorage.clear())
    );
  }

  getUser(): string {
    return localStorage.getItem('user') ?? '';
  }

  isAuthenticated(): boolean {
    return localStorage.getItem('user') !== null;
  }
}