import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-header.component',
  imports: [RouterModule, CommonModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent {
   private auth = inject(AuthService);
  private router = inject(Router);

  dropdownOpen = false;

  get userName(): string {
    return this.auth.getUser();
  }

  toggleMenu(): void {
    this.dropdownOpen = !this.dropdownOpen;
  }

  logout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }

}
