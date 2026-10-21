export const VehicleCategory = {
  SEDAN: 'SEDAN',
  HATCHBACK: 'HATCHBACK',
  SUV: 'SUV',
  PREMIUM: 'PREMIUM',
} as const;

export type VehicleCategory = (typeof VehicleCategory)[keyof typeof VehicleCategory];

export type RideStatus =
  | 'SCHEDULED'
  | 'REQUESTED'
  | 'ACCEPTED'
  | 'DRIVER_ARRIVING'
  | 'DRIVER_ARRIVED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED';

export interface LocationPoint {
  address?: string;
  lat: number;
  lng: number;
  latitude?: number;
  longitude?: number;
  label?: string;
}

export interface RideEventDto {
  eventType: string;
  actorRole: string;
  description: string;
  createdAt: string;
}

export interface Ride {
  id: number | string;
  riderId?: number | string;
  riderName?: string;
  riderPhone?: string;
  driverId?: number | string;
  driverName?: string;
  driverPhone?: string;
  driverRating?: number;
  rated?: boolean;
  vehicleInfo?: string;
  licensePlate?: string;
  pickupAddress: string;
  pickupLatitude?: number;
  pickupLongitude?: number;
  pickupLat?: number;
  pickupLng?: number;
  dropoffAddress: string;
  dropoffLatitude?: number;
  dropoffLongitude?: number;
  dropoffLat?: number;
  dropoffLng?: number;
  vehicleCategory: VehicleCategory;
  distanceKm?: number;
  estimatedDurationMinutes?: number;
  durationMin?: number;
  baseFare?: number;
  discountAmount?: number;
  estimatedFare?: number;
  finalFare?: number;
  currency?: string;
  rideStatus: RideStatus;
  status: RideStatus;
  cancellationReason?: string;
  cancelledBy?: string;
  requestedAt: string;
  scheduledFor?: string;
  acceptedAt?: string;
  arrivedAt?: string;
  startedAt?: string;
  completedAt?: string;
  cancelledAt?: string;
  events?: RideEventDto[];
}

export interface FareEstimate {
  vehicleCategory: VehicleCategory;
  distanceKm: number;
  estimatedDurationMinutes: number;
  durationMin?: number;
  baseFare: number;
  distanceCharge: number;
  timeCharge: number;
  serviceFee: number;
  surgeMultiplier: number;
  discountAmount: number;
  totalEstimatedFare: number;
  estimatedFare?: number;
  currency: string;
}

export interface CreateRidePayload {
  pickupAddress: string;
  pickupLatitude?: number;
  pickupLongitude?: number;
  pickupLat?: number;
  pickupLng?: number;
  dropoffAddress: string;
  dropoffLatitude?: number;
  dropoffLongitude?: number;
  dropoffLat?: number;
  dropoffLng?: number;
  vehicleCategory: VehicleCategory;
  promoCode?: string;
  scheduledFor?: string;
}
