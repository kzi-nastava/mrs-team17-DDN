export type RideStatus = 'ACCEPTED' | 'COMPLETED' | 'CANCELED' | 'ACTIVE';

export interface DriverRideHistoryItem {
  rideId: number;
  startedAt: string; // ISO string (OffsetDateTime)
  startAddress: string;
  endAddress: string;
  canceled: boolean;
  status: RideStatus;
  price: number;
}

export interface PassengerInfo {
  name: string;
  email: string;
}

export interface RideReport {
  id: number;
  rideId: number;
  description: string;
  createdAt: string; // ISO string
}

export interface DriverRideDetails {
  rideId: number;
  startedAt: string;
  endedAt: string | null;

  startAddress: string;
  destinationAddress: string;
  stops: string[];

  canceled: boolean;
  canceledBy: string | null;

  status: RideStatus;

  price: number;
  panicTriggered: boolean;

  passengers: PassengerInfo[];

  reports: RideReport[];
}
