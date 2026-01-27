export interface Ride {
  fromAddress: string;
  toAddress: string;

  fromLat: number;
  fromLng: number;
  toLat: number;
  toLng: number;

  distanceKm: number;
  durationMin: number;
  priceRsd: number;

  status: 'searching' | 'accepted' | 'arriving' | 'in_progress' | 'completed' | 'cancelled';

  createdAt: Date;
}