export type RideRatingRequest = {
  driverRating: number;   // 1..5
  vehicleRating: number;  // 1..5
  comment?: string;
};

export type RideRatingResponse = {
  rideId: number;
  driverRating: number;
  vehicleRating: number;
  comment?: string;
  createdAt: string;
};
