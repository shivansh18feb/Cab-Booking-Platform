import { VehicleCategory } from './ride';

export type DriverAvailability = 'ONLINE' | 'OFFLINE' | 'BUSY';
export type VerificationStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface Vehicle {
  id?: number | string;
  vehicleType?: VehicleCategory;
  category?: VehicleCategory;
  make: string;
  model: string;
  year: number;
  registrationNumber: string;
  color: string;
  seatingCapacity: number;
}

export interface DriverProfile {
  id: number | string;
  userId?: number | string;
  user?: {
    id: number | string;
    fullName: string;
    email: string;
    phoneNumber?: string;
  };
  fullName: string;
  email: string;
  phoneNumber?: string;
  licenseNumber: string;
  verificationStatus: VerificationStatus;
  availabilityStatus: DriverAvailability;
  availability?: DriverAvailability;
  currentLatitude?: number;
  currentLongitude?: number;
  averageRating: number;
  totalCompletedRides: number;
  totalRides?: number;
  vehicles: Vehicle[];
  vehicle?: Vehicle;
}

export interface DriverEarnings {
  totalGrossEarnings: number;
  totalEarnings?: number;
  todayEarnings?: number;
  weekEarnings?: number;
  monthEarnings?: number;
  platformCommission: number;
  netEarnings: number;
  completedRidesCount: number;
  totalRides?: number;
  todayRides?: number;
  weekRides?: number;
  avgPerRide?: number;
  cancellations?: number;
  acceptanceRate?: number;
  averageRating: number;
  recentRides?: any[];
}
