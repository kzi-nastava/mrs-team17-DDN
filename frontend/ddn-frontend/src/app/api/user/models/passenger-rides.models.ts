export interface PassengerRideHistoryItem {
  rideId: number;
  startedAt: string;
  startAddress: string;
  endedAt: string;
  destinationAddress: string;
  stops: string[];
}
