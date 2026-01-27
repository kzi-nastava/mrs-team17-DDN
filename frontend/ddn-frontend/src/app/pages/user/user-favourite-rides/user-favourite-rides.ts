import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import {
  FavoriteRoutesApiService,
  FavoriteRouteAnyDto,
  isFavoriteRouteNew
} from '../../../api/user/favorite-routes.http-data-source';

type FavouriteRideRow = {
  id: number;
  from: string;
  to: string;
};

@Component({
  selector: 'app-user-favourite-rides',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './user-favourite-rides.html',
  styleUrl: './user-favourite-rides.css',
})
export class UserFavouriteRides implements OnInit {
  readonly userId = 3003;

  favourites: FavouriteRideRow[] = [];
  isLoading = false;
  errorMsg = '';

  removingId: number | null = null;

  constructor(private api: FavoriteRoutesApiService) {}

  ngOnInit(): void {
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
            const from = this.shortAddr(x.start?.address);
            const to = this.shortAddr(x.destination?.address);
            return { id, from, to };
          }

          const from = this.shortAddr(x.startAddress);
          const to = this.shortAddr(x.destinationAddress);
          return { id, from, to };
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

  private shortAddr(s: string): string {
    const t = (s || '').trim();
    if (!t) return '—';
    const idx = t.indexOf(',');
    return idx > 0 ? t.slice(0, idx) : t;
  }

  private extractMsg(err: HttpErrorResponse, fallback: string): string {
    return (
      (err as any)?.error?.message ||
      (typeof (err as any)?.error === 'string' ? (err as any).error : '') || fallback
    );
  }
}
