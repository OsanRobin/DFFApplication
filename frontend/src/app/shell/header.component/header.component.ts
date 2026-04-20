import { CommonModule } from '@angular/common';
import { Component, HostListener, inject } from '@angular/core';
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
  showLogoutConfirm = false;

  get userName(): string {
    return this.auth.getUser();
  }

  toggleMenu(event: MouseEvent): void {
    event.stopPropagation();
    this.dropdownOpen = !this.dropdownOpen;
  }

  openLogoutConfirm(event: MouseEvent): void {
    event.stopPropagation();
    this.dropdownOpen = false;
    this.showLogoutConfirm = true;
  }

  closeLogoutConfirm(): void {
    this.showLogoutConfirm = false;
  }

  confirmLogout(): void {
    this.showLogoutConfirm = false;
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }

  @HostListener('document:click')
  closeDropdown(): void {
    this.dropdownOpen = false;
  }
}