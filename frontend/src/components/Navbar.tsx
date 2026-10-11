import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../services/api';
import {
  Car,
  Compass,
  History,
  Shield,
  LogOut,
  Menu,
  X,
  CreditCard,
  MapPin,
  LifeBuoy,
  Bell,
} from 'lucide-react';

export const Navbar: React.FC = () => {
  const { user, isAuthenticated, isRider, isDriver, isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const { data: unreadData } = useQuery({
    queryKey: ['notifications', 'unread-count'],
    queryFn: async () => {
      const res = await apiClient.get('/notifications/unread-count');
      return res.data.data as { unreadCount: number };
    },
    enabled: isAuthenticated,
    refetchInterval: 30000,
  });
  const unreadCount = unreadData?.unreadCount ?? 0;

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <nav className="bg-slate-900 border-b border-slate-800 text-white sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo & Brand */}
          <Link to="/" className="flex items-center space-x-3 group">
            <div className="h-10 w-10 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-400 flex items-center justify-center shadow-lg shadow-emerald-500/20 group-hover:scale-105 transition-transform">
              <Car className="h-6 w-6 text-white" />
            </div>
            <span className="text-xl font-bold tracking-tight bg-gradient-to-r from-white to-slate-300 bg-clip-text text-transparent">
              Cab<span className="text-emerald-400">Flow</span>
            </span>
          </Link>

          {/* Desktop Nav Items */}
          <div className="hidden md:flex items-center space-x-6">
            {isAuthenticated ? (
              <>
                {isRider && (
                  <>
                    <Link
                      to="/rider/dashboard"
                      className="text-slate-300 hover:text-white flex items-center space-x-1.5 text-sm font-medium transition"
                    >
                      <Compass className="h-4 w-4 text-emerald-400" />
                      <span>Book Ride</span>
                    </Link>
                    <Link
                      to="/rider/history"
                      className="text-slate-300 hover:text-white flex items-center space-x-1.5 text-sm font-medium transition"
                    >
                      <History className="h-4 w-4" />
                      <span>My Rides</span>
                    </Link>
                    <Link
                      to="/rider/saved-places"
                      className="text-slate-300 hover:text-white flex items-center space-x-1.5 text-sm font-medium transition"
                    >
                      <MapPin className="h-4 w-4" />
                      <span>Saved Places</span>
                    </Link>
                  </>
                )}

                {isDriver && (
                  <>
                    <Link
                      to="/driver/dashboard"
                      className="text-slate-300 hover:text-white flex items-center space-x-1.5 text-sm font-medium transition"
                    >
                      <Car className="h-4 w-4 text-emerald-400" />
                      <span>Driver Panel</span>
                    </Link>
                    <Link
                      to="/driver/earnings"
                      className="text-slate-300 hover:text-white flex items-center space-x-1.5 text-sm font-medium transition"
                    >
                      <CreditCard className="h-4 w-4 text-emerald-400" />
                      <span>Earnings</span>
                    </Link>
                  </>
                )}

                {isAdmin && (
                  <Link
                    to="/admin/dashboard"
                    className="text-amber-400 hover:text-amber-300 flex items-center space-x-1.5 text-sm font-medium transition"
                  >
                    <Shield className="h-4 w-4" />
                    <span>Admin Portal</span>
                  </Link>
                )}

                <Link
                  to="/support"
                  className="text-slate-300 hover:text-white flex items-center space-x-1.5 text-sm font-medium transition"
                >
                  <LifeBuoy className="h-4 w-4" />
                  <span>Support</span>
                </Link>

                {/* User badge & Logout */}
                <div className="flex items-center space-x-3 pl-4 border-l border-slate-700">
                  {/* Notification Bell */}
                  <Link
                    to="/notifications"
                    className="relative p-1.5 text-slate-400 hover:text-white transition"
                    title="Notifications"
                  >
                    <Bell className="h-5 w-5" />
                    {unreadCount > 0 && (
                      <span className="absolute -top-0.5 -right-0.5 h-4 w-4 text-[10px] font-bold bg-rose-500 text-white rounded-full flex items-center justify-center">
                        {unreadCount > 9 ? '9+' : unreadCount}
                      </span>
                    )}
                  </Link>

                  <div className="text-right">
                    <p className="text-xs font-semibold text-white leading-tight">{user?.fullName}</p>
                    <span className="text-[10px] uppercase font-bold tracking-wider px-1.5 py-0.5 rounded bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                      {user?.role}
                    </span>
                  </div>
                  <button
                    onClick={handleLogout}
                    className="p-2 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 transition"
                    title="Log Out"
                  >
                    <LogOut className="h-5 w-5" />
                  </button>
                </div>
              </>
            ) : (
              <>
                <Link
                  to="/login"
                  className="text-slate-300 hover:text-white text-sm font-medium transition"
                >
                  Sign In
                </Link>
                <Link
                  to="/register"
                  className="bg-emerald-500 hover:bg-emerald-600 text-white px-4 py-2 rounded-xl text-sm font-semibold transition shadow-md shadow-emerald-500/20"
                >
                  Ride with Us
                </Link>
                <Link
                  to="/driver/register"
                  className="bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 px-3.5 py-2 rounded-xl text-sm font-medium transition"
                >
                  Drive with Us
                </Link>
              </>
            )}
          </div>

          {/* Mobile menu toggle */}
          <div className="md:hidden flex items-center">
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="p-2 rounded-lg text-slate-400 hover:text-white"
            >
              {mobileMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Drawer */}
      {mobileMenuOpen && (
        <div className="md:hidden bg-slate-900 border-b border-slate-800 px-4 pt-2 pb-6 space-y-3">
          {isAuthenticated ? (
            <>
              <div className="py-2 border-b border-slate-800 flex items-center justify-between">
                <div>
                  <p className="text-sm font-bold text-white">{user?.fullName}</p>
                  <p className="text-xs text-slate-400">{user?.email}</p>
                </div>
                <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400">
                  {user?.role}
                </span>
              </div>
              {isRider && (
                <>
                  <Link
                    to="/rider/dashboard"
                    onClick={() => setMobileMenuOpen(false)}
                    className="block text-slate-300 py-1.5"
                  >
                    Book a Ride
                  </Link>
                  <Link
                    to="/rider/history"
                    onClick={() => setMobileMenuOpen(false)}
                    className="block text-slate-300 py-1.5"
                  >
                    Ride History
                  </Link>
                  <Link
                    to="/rider/saved-places"
                    onClick={() => setMobileMenuOpen(false)}
                    className="block text-slate-300 py-1.5"
                  >
                    Saved Places
                  </Link>
                </>
              )}
              {isDriver && (
                <>
                  <Link
                    to="/driver/dashboard"
                    onClick={() => setMobileMenuOpen(false)}
                    className="block text-slate-300 py-1.5"
                  >
                    Driver Dashboard
                  </Link>
                  <Link
                    to="/driver/earnings"
                    onClick={() => setMobileMenuOpen(false)}
                    className="block text-slate-300 py-1.5"
                  >
                    Earnings
                  </Link>
                </>
              )}
              {isAdmin && (
                <Link
                  to="/admin/dashboard"
                  onClick={() => setMobileMenuOpen(false)}
                  className="block text-amber-400 py-1.5 font-medium"
                >
                  Admin Portal
                </Link>
              )}
              <Link
                to="/support"
                onClick={() => setMobileMenuOpen(false)}
                className="block text-slate-300 py-1.5"
              >
                Help & Support
              </Link>
              <button
                onClick={() => {
                  setMobileMenuOpen(false);
                  handleLogout();
                }}
                className="w-full text-left text-rose-400 py-2 font-medium"
              >
                Log Out
              </button>
            </>
          ) : (
            <div className="space-y-2 pt-2">
              <Link
                to="/login"
                onClick={() => setMobileMenuOpen(false)}
                className="block text-center bg-slate-800 text-white py-2 rounded-xl"
              >
                Sign In
              </Link>
              <Link
                to="/register"
                onClick={() => setMobileMenuOpen(false)}
                className="block text-center bg-emerald-500 text-white py-2 rounded-xl font-medium"
              >
                Register as Rider
              </Link>
              <Link
                to="/driver/register"
                onClick={() => setMobileMenuOpen(false)}
                className="block text-center border border-slate-700 text-slate-300 py-2 rounded-xl"
              >
                Register as Driver
              </Link>
            </div>
          )}
        </div>
      )}
    </nav>
  );
};

export default Navbar;
