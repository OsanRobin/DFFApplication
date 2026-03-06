import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface CurrentUser {
  user: string;
  organization: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private http = inject(HttpClient);

  login(): Observable<string> {
    return this.http.post('http://localhost:8081/api/auth/login', {}, { responseType: 'text' });
  }

  getCurrentUser(): Observable<CurrentUser> {
    return this.http.get<CurrentUser>('http://localhost:8081/api/auth/me').pipe(
      tap(user => {
        localStorage.setItem('user', user.user);
        localStorage.setItem('organization', user.organization);
      })
    );
  }

  logout(): void {
    localStorage.clear();
  }

  getUser(): string {
    return localStorage.getItem('user') ?? '';
  }

  isAuthenticated(): boolean {
    return localStorage.getItem('user') !== null;
  }
}