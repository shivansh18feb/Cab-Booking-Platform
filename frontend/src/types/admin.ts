export interface AdminDashboardStats {
  totalUsers: number;
  totalRiders: number;
  totalDrivers: number;
  verifiedDrivers: number;
  pendingDrivers: number;
  pendingDriverVerification?: number;
  totalRides: number;
  activeRides: number;
  completedRides: number;
  cancelledRides: number;
  totalGrossRevenue: number;
  totalRevenue?: number;
  pendingTickets: number;
  openSupportTickets?: number;
}
