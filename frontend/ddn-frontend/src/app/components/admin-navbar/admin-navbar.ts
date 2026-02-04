import { CommonModule } from '@angular/common';
import { Component, ElementRef, HostListener, ViewChild, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthStore } from '../../api/auth/auth.store';

@Component({
  selector: 'app-admin-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './admin-navbar.html',
  styleUrl: './admin-navbar.css',
})
export class AdminNavbar {
  private auth = inject(AuthStore);
  private router = inject(Router);

  moreOpen = false;

  // pozicija dropdown-a (fixed)
  moreLeft = 0;
  moreTop = 0;

  @ViewChild('moreBtn', { read: ElementRef })
  moreBtn?: ElementRef<HTMLElement>;

  logout(): void {
    this.auth.clear();
    this.router.navigate(['/login']);
  }

  toggleMore(ev: MouseEvent): void {
    ev.stopPropagation();

    this.moreOpen = !this.moreOpen;
    if (this.moreOpen) {
      this.repositionMore();
    }
  }

  closeMore(): void {
    this.moreOpen = false;
  }

  private repositionMore(): void {
    const el = this.moreBtn?.nativeElement;
    if (!el) return;

    const r = el.getBoundingClientRect();
    this.moreLeft = r.left;
    this.moreTop = r.bottom + 8; // 8px ispod dugmeta
  }

  @HostListener('window:resize')
  onResize(): void {
    if (this.moreOpen) this.repositionMore();
  }

  @HostListener('window:scroll')
  onScroll(): void {
    if (this.moreOpen) this.repositionMore();
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    this.moreOpen = false;
  }
}
