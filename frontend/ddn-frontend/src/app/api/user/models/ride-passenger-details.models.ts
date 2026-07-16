export interface LatLng {
  lat: number;
  lng: number;
}

export interface RideCheckpoint {
  stopOrder: number;
  address: string;
  lat: number;
  lng: number;
}

export interface RideReport {
  description: string;
  created_at: string; // ISO timestamp, matches backend RideReportDto field name
}

export interface RideRating {
  id: number;
  rideId: number;
  driverRating: number;
  vehicleRating: number;
  comment: string | null;
  createdAt: string;
}

export interface DriverPublicInfo {
  driverId: number;
  firstName: string;
  lastName: string;
  profileImageUrl: string | null;
  vehicleModel: string | null;
  vehicleType: string | null;
  licensePlate: string | null;
}

// Matches backend RidePassengerDetailsResponseDto returned by
// GET /api/rides/{rideId}/passenger-details
export interface RidePassengerDetails {
  rideId: number;
  status: string;

  startedAt: string | null;
  endedAt: string | null;

  startAddress: string;
  destinationAddress: string;

  start: LatLng | null;
  destination: LatLng | null;

  stops: RideCheckpoint[];

  route: LatLng[];
  distanceKm: number;

  reports: RideReport[];

  rating: RideRating | null;
  driver: DriverPublicInfo | null;

  vehicleType: string;
  babyTransport: boolean;
  petTransport: boolean;
}
