export type RideStatus = 'COMPLETED' | 'CANCELED' | 'ACTIVE';

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

  status: RideStatus; // ✅ DODATO — OVO FIXUJE TS2339

  price: number;
  panicTriggered: boolean;

  passengers: PassengerInfo[];

  reports: RideReport[];
}
