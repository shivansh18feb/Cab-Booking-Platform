export type Role = 'RIDER' | 'DRIVER' | 'ADMIN';
export type AccountStatus = 'ACTIVE' | 'SUSPENDED' | 'INACTIVE';

export interface User {
  id: number;
  fullName: string;
  email: string;
  phoneNumber: string;
  role: Role;
  accountStatus: AccountStatus;
  emailVerified: boolean;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInMs: number;
  user: User;
}
