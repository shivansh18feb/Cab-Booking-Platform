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

export interface SupportTicket {
  id: number;
  createdById: number;
  createdByName: string;
  rideId?: number;
  subject: string;
  description: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
  assignedAdminId?: number;
  assignedAdminName?: string;
  resolutionNotes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface AdminPromoCode {
  id: number;
  code: string;
  discountType: 'PERCENTAGE' | 'FIXED_AMOUNT';
  discountValue: number;
  minimumFare: number;
  maximumDiscount?: number;
  validFrom: string;
  validUntil: string;
  usageLimit: number;
  timesUsed: number;
  active: boolean;
}

export interface PromoRedemption {
  id: number;
  promoCodeId: number;
  promoCode: string;
  userId: number;
  userName: string;
  userEmail: string;
  rideId: number;
  discountAmount: number;
  redeemedAt: string;
}

export interface AdminReview {
  id: number;
  rideId: number;
  riderId: number;
  riderName: string;
  driverId: number;
  driverName: string;
  stars: number;
  comment?: string;
  hidden: boolean;
  moderatedAt?: string;
  createdAt: string;
}

export interface AuditLogEntry {
  id: number;
  actorId?: number;
  actorEmail?: string;
  actorRole?: string;
  action: string;
  resourceType: string;
  resourceId?: string;
  details?: string;
  ipAddress?: string;
  createdAt: string;
}
