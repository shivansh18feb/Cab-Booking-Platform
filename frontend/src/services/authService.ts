import { apiClient } from './api';
import { AuthResponse, User } from '../types/auth';

export const authService = {
  async register(data: {
    fullName: string;
    email: string;
    phoneNumber: string;
    password: string;
  }): Promise<AuthResponse> {
    const res = await apiClient.post('/auth/register', data);
    return res.data.data;
  },

  async registerDriver(data: {
    fullName: string;
    email: string;
    phoneNumber: string;
    password: string;
    licenseNumber: string;
    vehicleType: string;
    make: string;
    model: string;
    year: number;
    registrationNumber: string;
    color: string;
    seatingCapacity: number;
  }): Promise<AuthResponse> {
    const res = await apiClient.post('/auth/driver/register', data);
    return res.data.data;
  },

  async login(data: { email: string; password: string }): Promise<AuthResponse> {
    const res = await apiClient.post('/auth/login', data);
    return res.data.data;
  },

  async logout(): Promise<void> {
    const refreshToken = localStorage.getItem('refreshToken');
    try {
      await apiClient.post('/auth/logout', { refreshToken });
    } catch {
      // Ignore network errors on logout
    } finally {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
    }
  },

  async getCurrentUser(): Promise<User> {
    const res = await apiClient.get('/auth/me');
    return res.data.data;
  },
};
