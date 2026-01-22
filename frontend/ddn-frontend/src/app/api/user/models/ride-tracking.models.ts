export type LatLng = { lat: number; lng: number };

export type TrackingState = {
  car: LatLng;
  pickup: LatLng;
  destination: LatLng;
  route: LatLng[]; // NEW
  etaMinutes: number;
  distanceKm: number;
  status: string;
};

export type InconsistencyReport = {
  rideId: number;
  text: string;
  createdAt: string;
};
