import { apiClient } from './api';
import { CreateRidePayload, FareEstimate, Ride, VehicleCategory } from '../types/ride';

interface GetEstimateParams {
  pickupLatitude?: number;
  pickupLongitude?: number;
  pickupLat?: number;
  pickupLng?: number;
  dropoffLatitude?: number;
  dropoffLongitude?: number;
  dropoffLat?: number;
  dropoffLng?: number;
  vehicleCategory: VehicleCategory;
  promoCode?: string;
}

const normalizeRide = (ride: any): Ride => {
  if (!ride) return ride;
  return {
    ...ride,
    status: ride.status || ride.rideStatus || 'REQUESTED',
    rideStatus: ride.rideStatus || ride.status || 'REQUESTED',
    pickupLat: ride.pickupLat ?? ride.pickupLatitude,
    pickupLng: ride.pickupLng ?? ride.pickupLongitude,
    dropoffLat: ride.dropoffLat ?? ride.dropoffLatitude,
    dropoffLng: ride.dropoffLng ?? ride.dropoffLongitude,
    durationMin: ride.durationMin ?? ride.estimatedDurationMinutes,
  };
};

export const rideService = {
  async getEstimate(data: GetEstimateParams): Promise<FareEstimate> {
    const payload = {
      pickupLatitude: data.pickupLatitude ?? data.pickupLat,
      pickupLongitude: data.pickupLongitude ?? data.pickupLng,
      dropoffLatitude: data.dropoffLatitude ?? data.dropoffLat,
      dropoffLongitude: data.dropoffLongitude ?? data.dropoffLng,
      vehicleCategory: data.vehicleCategory,
      promoCode: data.promoCode,
    };
    const res = await apiClient.post('/rides/estimate', payload);
    const est = res.data.data;
    return {
      ...est,
      durationMin: est.durationMin ?? est.estimatedDurationMinutes,
      estimatedFare: est.estimatedFare ?? est.totalEstimatedFare,
    };
  },

  async createRide(payload: CreateRidePayload): Promise<Ride> {
    const body = {
      pickupAddress: payload.pickupAddress,
      pickupLatitude: payload.pickupLatitude ?? payload.pickupLat,
      pickupLongitude: payload.pickupLongitude ?? payload.pickupLng,
      dropoffAddress: payload.dropoffAddress,
      dropoffLatitude: payload.dropoffLatitude ?? payload.dropoffLat,
      dropoffLongitude: payload.dropoffLongitude ?? payload.dropoffLng,
      vehicleCategory: payload.vehicleCategory,
      promoCode: payload.promoCode,
      scheduledFor: payload.scheduledFor,
    };
    const res = await apiClient.post('/rides', body);
    return normalizeRide(res.data.data);
  },

  async getRide(id: number | string): Promise<Ride> {
    const res = await apiClient.get(`/rides/${id}`);
    return normalizeRide(res.data.data);
  },

  async getActiveRide(): Promise<Ride | null> {
    const res = await apiClient.get('/rides/active');
    return res.data.data ? normalizeRide(res.data.data) : null;
  },

  async cancelRide(id: number | string, reason?: string): Promise<Ride> {
    const res = await apiClient.post(`/rides/${id}/cancel`, { reason });
    return normalizeRide(res.data.data);
  },

  async getRideHistory(page = 0, size = 10): Promise<{ content: Ride[]; totalPages: number; totalElements: number }> {
    const res = await apiClient.get(`/rides/history?page=${page}&size=${size}`);
    const data = res.data.data;
    return {
      ...data,
      content: (data.content || []).map(normalizeRide),
    };
  },

  getReceiptDownloadUrl(rideId: number | string): string {
    const token = localStorage.getItem('accessToken');
    return `/api/v1/rides/${rideId}/receipt?token=${token}`;
  },

  async submitRating(rideId: number | string, stars: number, comment?: string): Promise<void> {
    await apiClient.post(`/rides/${rideId}/rating`, { stars, comment });
  },

  async triggerSos(rideId: number | string, note?: string, latitude?: number, longitude?: number): Promise<void> {
    await apiClient.post(`/rides/${rideId}/sos`, { note, latitude, longitude });
  },
};

export default rideService;
