import { apiClient } from './api';
import { AdminDashboardStats } from '../types/admin';
import { User, AccountStatus } from '../types/auth';
import { DriverProfile, VerificationStatus } from '../types/driver';
import { Ride } from '../types/ride';

export const adminService = {
  async getDashboardStats(): Promise<AdminDashboardStats> {
    const res = await apiClient.get('/admin/dashboard');
    const data = res.data.data;
    return {
      ...data,
      totalRevenue: data.totalGrossRevenue ?? 0,
      pendingDriverVerification: data.pendingDrivers ?? 0,
      openSupportTickets: data.pendingTickets ?? 0,
    };
  },

  async getUsers(page = 0, size = 10, search?: string): Promise<{ content: User[]; totalPages: number; totalElements: number }> {
    const searchParam = search ? `&search=${encodeURIComponent(search)}` : '';
    const res = await apiClient.get(`/admin/users?page=${page}&size=${size}${searchParam}`);
    return res.data.data;
  },

  async updateUserStatus(userId: number | string, status: AccountStatus | string): Promise<User> {
    const res = await apiClient.patch(`/admin/users/${userId}/status?status=${status}`);
    return res.data.data;
  },

  async getPendingDrivers(): Promise<DriverProfile[]> {
    const res = await apiClient.get('/admin/drivers/pending');
    return res.data.data;
  },

  async verifyDriver(driverId: number | string, statusOrApproved: VerificationStatus | boolean | string): Promise<DriverProfile> {
    const status = typeof statusOrApproved === 'boolean'
      ? (statusOrApproved ? 'APPROVED' : 'REJECTED')
      : statusOrApproved;
    const res = await apiClient.post(`/admin/drivers/${driverId}/verify?status=${status}`);
    return res.data.data;
  },

  async getAllRides(page = 0, size = 10, status?: string): Promise<{ content: Ride[]; totalPages: number; totalElements: number }> {
    const statusParam = status ? `&status=${encodeURIComponent(status)}` : '';
    const res = await apiClient.get(`/admin/rides?page=${page}&size=${size}${statusParam}`);
    const data = res.data.data;
    return {
      ...data,
      content: (data.content || []).map((r: any) => ({
        ...r,
        status: r.status || r.rideStatus || 'REQUESTED',
        rideStatus: r.rideStatus || r.status || 'REQUESTED',
      })),
    };
  },

  async getAllTickets(page = 0, size = 10, status?: string): Promise<{ content: any[]; totalPages: number; totalElements: number }> {
    const statusParam = status ? `&status=${encodeURIComponent(status)}` : '';
    const res = await apiClient.get(`/admin/tickets?page=${page}&size=${size}${statusParam}`);
    return res.data.data;
  },

  async updateTicketStatus(ticketId: number | string, data: { status: string; resolutionNotes?: string; assignedAdminId?: number }): Promise<any> {
    const res = await apiClient.patch(`/admin/tickets/${ticketId}/status`, data);
    return res.data.data;
  },

  async getPromotions(page = 0, size = 10): Promise<{ content: any[]; totalPages: number; totalElements: number }> {
    const res = await apiClient.get(`/admin/promotions?page=${page}&size=${size}`);
    return res.data.data;
  },

  async createPromotion(data: any): Promise<any> {
    const res = await apiClient.post('/admin/promotions', data);
    return res.data.data;
  },

  async togglePromotion(promoId: number | string): Promise<any> {
    const res = await apiClient.patch(`/admin/promotions/${promoId}/toggle`);
    return res.data.data;
  },

  async deletePromotion(promoId: number | string): Promise<void> {
    await apiClient.delete(`/admin/promotions/${promoId}`);
  },

  async getPromoRedemptions(promoId: number | string, page = 0, size = 10): Promise<{ content: any[]; totalPages: number; totalElements: number }> {
    const res = await apiClient.get(`/admin/promotions/${promoId}/redemptions?page=${page}&size=${size}`);
    return res.data.data;
  },

  async getReviews(page = 0, size = 10, isHidden?: boolean, stars?: number): Promise<{ content: any[]; totalPages: number; totalElements: number }> {
    const hiddenParam = isHidden !== undefined ? `&isHidden=${isHidden}` : '';
    const starsParam = stars !== undefined ? `&stars=${stars}` : '';
    const res = await apiClient.get(`/admin/reviews?page=${page}&size=${size}${hiddenParam}${starsParam}`);
    return res.data.data;
  },

  async toggleReviewVisibility(ratingId: number | string, hide: boolean): Promise<any> {
    const res = await apiClient.patch(`/admin/reviews/${ratingId}/visibility?hide=${hide}`);
    return res.data.data;
  },

  async deleteReview(ratingId: number | string): Promise<void> {
    await apiClient.delete(`/admin/reviews/${ratingId}`);
  },

  async getAuditLogs(page = 0, size = 10, action?: string, resourceType?: string): Promise<{ content: any[]; totalPages: number; totalElements: number }> {
    const actionParam = action ? `&action=${encodeURIComponent(action)}` : '';
    const typeParam = resourceType ? `&resourceType=${encodeURIComponent(resourceType)}` : '';
    const res = await apiClient.get(`/admin/audit-logs?page=${page}&size=${size}${actionParam}${typeParam}`);
    return res.data.data;
  },
};

export default adminService;
