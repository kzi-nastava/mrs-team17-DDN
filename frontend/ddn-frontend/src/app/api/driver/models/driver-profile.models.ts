export type UserRole = 'DRIVER' | 'PASSENGER' | 'ADMIN';

export interface UserProfileResponseDto {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  address: string;
  phoneNumber: string;
  profileImageUrl: string;
  role: UserRole;
}

export interface VehicleInfoResponseDto {
  model: string;
  type: string;
  licensePlate: string;
  seats: number;
  babyTransport: boolean;
  petTransport: boolean;
}

export interface DriverProfileResponseDto {
  driver: UserProfileResponseDto;
  vehicle: VehicleInfoResponseDto;
  activeMinutesLast24h: number;
}

export interface UpdateDriverProfileRequestDto {
  firstName?: string;
  lastName?: string;
  address?: string;
  phoneNumber?: string;
  profileImageUrl?: string;
}

export interface ProfileChangeRequestResponseDto {
  requestId: number;
  driverId: number;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  createdAt: string;
}
