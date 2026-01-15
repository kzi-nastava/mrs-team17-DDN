import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

type FavouriteRideRow = {
  id: number;
  date: string;
  route: string;
  priceRsd: number;
};

@Component({
  selector: 'app-user-favourite-rides',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './user-favourite-rides.html',
  styleUrl: './user-favourite-rides.css',
})
export class UserFavouriteRides {
  favourites: FavouriteRideRow[] = [
    { id: 1, date: '13.12.2025', route: 'FTN → Železnička', priceRsd: 820 },
    { id: 2, date: '01.11.2025', route: 'Novi Sad → Beograd', priceRsd: 1500 },
  ];
}


