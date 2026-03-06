import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface LoginResponse {
  success: boolean;
  message: string;
  user: string | null;
  organization: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);

  login(): Observable<string> {
    return this.http.post('http://localhost:8081/api/auth/login', {}, { responseType: 'text' });
  }

  saveSession(): void {
    localStorage.setItem('isAuthenticated', 'true');
    localStorage.setItem('user', 'admin');
    localStorage.setItem('organization', 'DailyFreshFood-B1-Site');
  }

  logout(): void {
    localStorage.removeItem('isAuthenticated');
    localStorage.removeItem('user');
    localStorage.removeItem('organization');
  }

  isAuthenticated(): boolean {
    return localStorage.getItem('isAuthenticated') === 'true';
  }
}