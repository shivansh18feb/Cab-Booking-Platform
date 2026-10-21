import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { adminService } from '../../services/adminService';

export default function AdminDashboard() {
  const { data: stats, isLoading } = useQuery({
    queryKey: ['adminStats'],
    queryFn: adminService.getDashboardStats,
    refetchInterval: 30_000,
  });

  const cards = stats
    ? [
        { label: 'Total Users',      value: stats.totalUsers,      icon: '👥', color: 'bg-blue-50 text-blue-700',    border: 'border-blue-200' },
        { label: 'Total Drivers',    value: stats.totalDrivers,    icon: '🚗', color: 'bg-indigo-50 text-indigo-700', border: 'border-indigo-200' },
        { label: 'Total Rides',      value: stats.totalRides,      icon: '🛣️', color: 'bg-purple-50 text-purple-700', border: 'border-purple-200' },
        { label: 'Active Rides',     value: stats.activeRides,     icon: '🔴', color: 'bg-red-50 text-red-700',       border: 'border-red-200' },
        { label: 'Completed Rides',  value: stats.completedRides,  icon: '✅', color: 'bg-green-50 text-green-700',   border: 'border-green-200' },
        { label: 'Revenue (Total)',  value: `₹${(stats.totalRevenue ?? 0).toFixed(0)}`, icon: '💰', color: 'bg-yellow-50 text-yellow-700', border: 'border-yellow-200' },
        { label: 'Pending Drivers',  value: stats.pendingDriverVerification, icon: '⏳', color: 'bg-orange-50 text-orange-700', border: 'border-orange-200' },
        { label: 'Open Tickets',     value: stats.openSupportTickets,        icon: '🎫', color: 'bg-pink-50 text-pink-700', border: 'border-pink-200' },
      ]
    : [];

  return (
    <div className="max-w-6xl mx-auto px-4 py-8 fade-in">
      <div className="flex items-center justify-between mb-6">
        <h1 className="page-title">Admin Dashboard</h1>
        <div className="flex items-center gap-2 text-sm text-gray-400">
          <span className="live-pulse w-2 h-2 bg-green-400 rounded-full inline-block" />
          Live
        </div>
      </div>

      {/* Stats Grid */}
      {isLoading ? (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
          {[...Array(8)].map((_, i) => <div key={i} className="skeleton h-28 rounded-xl" />)}
        </div>
      ) : (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
          {cards.map(card => (
            <div key={card.label} className={`card border ${card.border} ${card.color.split(' ')[0]}`}>
              <div className="text-3xl mb-2">{card.icon}</div>
              <p className={`text-2xl font-bold ${card.color.split(' ')[1]}`}>{card.value}</p>
              <p className="text-xs text-gray-500 mt-1">{card.label}</p>
            </div>
          ))}
        </div>
      )}

      {/* Navigation Links */}
      <h2 className="text-base font-semibold text-gray-900 mb-3">Management Consoles</h2>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {[
          { to: '/admin/users',       icon: '👥', label: 'Manage Users',       desc: 'View & block/activate registered accounts' },
          { to: '/admin/drivers',     icon: '🚗', label: 'Manage Drivers',     desc: 'Verify driver documents & licenses' },
          { to: '/admin/rides',       icon: '🛣️', label: 'All Rides',          desc: 'Monitor real-time & completed rides' },
          { to: '/admin/support',     icon: '🎫', label: 'Support Tickets',    desc: 'Triage, reply & resolve support tickets' },
          { to: '/admin/promotions',  icon: '🏷️', label: 'Promotions & Coupons', desc: 'Manage discounts, limits & track redemptions' },
          { to: '/admin/reviews',     icon: '⭐', label: 'Review Moderation',   desc: 'Moderate ratings & hide abusive reviews' },
          { to: '/admin/audit-logs',  icon: '📋', label: 'Audit Logs',         desc: 'Immutable security & activity trail' },
          { to: '/support',           icon: '💬', label: 'User Support Portal', desc: 'Direct user-facing support experience' },
        ].map(item => (
          <Link key={item.to} to={item.to} className="card-hover hover:border-indigo-200 p-4 border rounded-xl bg-white shadow-sm block">
            <div className="text-2xl mb-2">{item.icon}</div>
            <h3 className="font-semibold text-gray-900 text-sm mb-1">{item.label}</h3>
            <p className="text-xs text-gray-500">{item.desc}</p>
          </Link>
        ))}
      </div>
    </div>
  );
}
