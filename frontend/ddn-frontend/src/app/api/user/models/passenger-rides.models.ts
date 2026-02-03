export interface PassengerRideHistoryItem {
  rideId: number;
  startedAt: string;
  startAddress: string;
  destinationAddress: string;
  stops: string[];
}
