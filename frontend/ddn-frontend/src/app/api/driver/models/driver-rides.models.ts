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

export interface DriverRideDetails {
  rideId: number;
  startedAt: string;
  endedAt: string;

  startAddress: string;
  destinationAddress: string;
  stops: string[];

  canceled: boolean;
  canceledBy: string | null;

  price: number;
  panicTriggered: boolean;

  passengers: PassengerInfo[];
}
