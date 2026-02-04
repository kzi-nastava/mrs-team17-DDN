export type LatLng = { lat: number; lng: number };

export type RideCheckpoint = {
  stopOrder: number;
  address: string;
  lat: number;
  lng: number;
};

export type TrackingState = {
  car: LatLng;
  pickup: LatLng;
  destination: LatLng;
  route: LatLng[];
  checkpoints?: RideCheckpoint[];
  etaMinutes: number;
  distanceKm: number;
  status: string;
};

export type InconsistencyReport = {
  rideId: number;
  text: string;
  createdAt: string;
};
