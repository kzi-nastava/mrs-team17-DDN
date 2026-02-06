export interface AdminUserStatusDto {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: 'DRIVER' | 'PASSENGER' | 'ADMIN' | string;
  blocked: boolean;
  blockReason?: string | null;
}

export interface AdminSetUserBlockRequestDto {
  blocked: boolean;
  blockReason?: string | null;
}
