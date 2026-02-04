import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { AuthStore } from '../../../api/auth/auth.store';
import {
  FavoriteRoutesApiService,
  FavoriteRouteAnyDto,
  isFavoriteRouteNew
} from '../../../api/user/favorite-routes.http-data-source';

type FavouriteRideRow = {
  id: number;
  from: string;     
  to: string;    
  fromFull: string;
  toFull: string;  
};

@Component({
  selector: 'app-user-favourite-rides',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './user-favourite-rides.html',
  styleUrl: './user-favourite-rides.css',
})
export class UserFavouriteRides implements OnInit {
  private readonly authStore = inject(AuthStore);
  private readonly router = inject(Router);

  userId!: number;

  favourites: FavouriteRideRow[] = [];
  isLoading = false;
  errorMsg = '';

  removingId: number | null = null;

  constructor(private api: FavoriteRoutesApiService) {}

  ngOnInit(): void {
    const id = this.authStore.getCurrentUserId();
    if (!id) {
      this.router.navigate(['/login']);
      return;
    }
    this.userId = id;
    this.load();
  }

  load(): void {
    this.isLoading = true;
    this.errorMsg = '';

    this.api.listFavorites(this.userId).subscribe({
      next: (list: FavoriteRouteAnyDto[]) => {
        this.favourites = (list || []).map((x: any) => {
          const id = Number(x.id);

          if (isFavoriteRouteNew(x)) {
            const fromFull = (x.start?.address || '').trim();
            const toFull = (x.destination?.address || '').trim();

            const from = this.displayAddr(fromFull);
            const to = this.displayAddr(toFull);

            return { id, from, to, fromFull, toFull };
          }

          const fromFull = (x.startAddress || '').trim();
          const toFull = (x.destinationAddress || '').trim();

          const from = this.displayAddr(fromFull);
          const to = this.displayAddr(toFull);

          return { id, from, to, fromFull, toFull };
        });

        this.isLoading = false;
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading = false;
        this.errorMsg = this.extractMsg(err, 'Failed to load favourite rides.');
      }
    });
  }

  remove(favoriteRouteId: number): void {
    if (this.removingId !== null) return;

    this.errorMsg = '';
    this.removingId = favoriteRouteId;

    this.api.removeFavorite(this.userId, favoriteRouteId).subscribe({
      next: () => {
        this.favourites = this.favourites.filter(x => x.id !== favoriteRouteId);
        this.removingId = null;
      },
      error: (err: HttpErrorResponse) => {
        this.removingId = null;
        this.errorMsg = this.extractMsg(err, 'Failed to remove favourite route.');
      }
    });
  }

  private displayAddr(s: string): string {
    const t = (s || '').trim();
    if (!t) return '—';

    const N = 42;

    if (t.length <= N) return t;

    return t.slice(0, N).trimEnd() + '...';
  }

  private extractMsg(err: HttpErrorResponse, fallback: string): string {
    return (
      (err as any)?.error?.message ||
      (typeof (err as any)?.error === 'string' ? (err as any).error : '') ||
      fallback
    );
  }
}
