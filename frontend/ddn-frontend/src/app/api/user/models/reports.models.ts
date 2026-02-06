export interface RideStatsTotalsDto {
  rides: number;
  kilometers: number;
  money: number;
}

export interface RideStatsAveragesDto {
  ridesPerDay: number;
  kilometersPerDay: number;
  moneyPerDay: number;

  kilometersPerRide: number;
  moneyPerRide: number;
}

export interface RideStatsPointDto {
  date: string;

  rides: number;
  kilometers: number;
  money: number;

  cumulativeRides: number;
  cumulativeKilometers: number;
  cumulativeMoney: number;
}

export interface RideStatsReportResponseDto {
  targetRole: 'DRIVER' | 'PASSENGER';
  scope: 'USER' | 'ALL';
  targetUserId?: number | null;

  from: string;
  to: string;
  days: number;

  points: RideStatsPointDto[];
  totals: RideStatsTotalsDto;
  averages: RideStatsAveragesDto;
}
