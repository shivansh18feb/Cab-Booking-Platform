import { lazy, Suspense } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import ProtectedRoute from './components/ProtectedRoute';
import { useAuth } from './context/AuthContext';

/* ── Lazy-loaded pages ── */
const LandingPage             = lazy(() => import('./pages/LandingPage'));
const LoginPage               = lazy(() => import('./pages/LoginPage'));
const RegisterPage            = lazy(() => import('./pages/RegisterPage'));
const DriverRegisterPage      = lazy(() => import('./pages/DriverRegisterPage'));

// Rider
const RiderDashboard          = lazy(() => import('./pages/rider/RiderDashboard'));
const BookRidePage            = lazy(() => import('./pages/rider/BookRidePage'));
const RideTrackingPage        = lazy(() => import('./pages/rider/RideTrackingPage'));
const RideHistoryPage         = lazy(() => import('./pages/rider/RideHistoryPage'));
const RideDetailPage          = lazy(() => import('./pages/rider/RideDetailPage'));
const SavedPlacesPage         = lazy(() => import('./pages/rider/SavedPlacesPage'));
const PromotionsPage          = lazy(() => import('./pages/rider/PromotionsPage'));
const RiderProfilePage        = lazy(() => import('./pages/rider/RiderProfilePage'));
const PaymentsHistoryPage     = lazy(() => import('./pages/rider/PaymentsHistoryPage'));

// Driver
const DriverDashboard         = lazy(() => import('./pages/driver/DriverDashboard'));
const DriverEarningsPage      = lazy(() => import('./pages/driver/DriverEarningsPage'));
const DriverProfilePage       = lazy(() => import('./pages/driver/DriverProfilePage'));

// Admin
const AdminDashboard          = lazy(() => import('./pages/admin/AdminDashboard'));
const AdminUsersPage          = lazy(() => import('./pages/admin/AdminUsersPage'));
const AdminDriversPage        = lazy(() => import('./pages/admin/AdminDriversPage'));
const AdminRidesPage          = lazy(() => import('./pages/admin/AdminRidesPage'));
const AdminSupportPage        = lazy(() => import('./pages/admin/AdminSupportPage'));
const AdminPromotionsPage     = lazy(() => import('./pages/admin/AdminPromotionsPage'));
const AdminReviewsPage        = lazy(() => import('./pages/admin/AdminReviewsPage'));
const AdminAuditLogsPage      = lazy(() => import('./pages/admin/AdminAuditLogsPage'));

// Misc
const SupportPage             = lazy(() => import('./pages/SupportPage'));
const TermsPage               = lazy(() => import('./pages/TermsPage'));
const AboutPage               = lazy(() => import('./pages/AboutPage'));
const ContactPage             = lazy(() => import('./pages/ContactPage'));
const PrivacyPage             = lazy(() => import('./pages/PrivacyPage'));
const NotificationsPage       = lazy(() => import('./pages/NotificationsPage'));

const PageLoader = () => (
  <div className="min-h-screen flex items-center justify-center bg-gray-50">
    <div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin" />
  </div>
);

export default function App() {
  const { user } = useAuth();

  return (
    <div className="flex flex-col min-h-screen">
      <Navbar />
      <main className="flex-1">
        <Suspense fallback={<PageLoader />}>
          <Routes>
            {/* Public */}
            <Route path="/" element={<LandingPage />} />
            <Route path="/login" element={user ? <Navigate to={roleHome(user.role)} replace /> : <LoginPage />} />
            <Route path="/register" element={user ? <Navigate to={roleHome(user.role)} replace /> : <RegisterPage />} />
            <Route path="/driver/register" element={user ? <Navigate to={roleHome(user.role)} replace /> : <DriverRegisterPage />} />
            <Route path="/terms" element={<TermsPage />} />
            <Route path="/terms-privacy" element={<TermsPage />} />
            <Route path="/about" element={<AboutPage />} />
            <Route path="/contact" element={<ContactPage />} />
            <Route path="/privacy" element={<PrivacyPage />} />
            <Route path="/support" element={<SupportPage />} />

            {/* Rider */}
            <Route path="/rider/dashboard" element={
              <ProtectedRoute roles={['RIDER']}><RiderDashboard /></ProtectedRoute>
            } />
            <Route path="/rider/book" element={
              <ProtectedRoute roles={['RIDER']}><BookRidePage /></ProtectedRoute>
            } />
            <Route path="/rider/ride/:id/track" element={
              <ProtectedRoute roles={['RIDER']}><RideTrackingPage /></ProtectedRoute>
            } />
            <Route path="/rider/history" element={
              <ProtectedRoute roles={['RIDER']}><RideHistoryPage /></ProtectedRoute>
            } />
            <Route path="/rider/ride/:id" element={
              <ProtectedRoute roles={['RIDER']}><RideDetailPage /></ProtectedRoute>
            } />
            <Route path="/rider/saved-places" element={
              <ProtectedRoute roles={['RIDER']}><SavedPlacesPage /></ProtectedRoute>
            } />
            <Route path="/rider/promotions" element={
              <ProtectedRoute roles={['RIDER']}><PromotionsPage /></ProtectedRoute>
            } />
            <Route path="/rider/profile" element={
              <ProtectedRoute roles={['RIDER']}><RiderProfilePage /></ProtectedRoute>
            } />
            <Route path="/rider/payments" element={
              <ProtectedRoute roles={['RIDER']}><PaymentsHistoryPage /></ProtectedRoute>
            } />

            {/* Driver */}
            <Route path="/driver/dashboard" element={
              <ProtectedRoute roles={['DRIVER']}><DriverDashboard /></ProtectedRoute>
            } />
            <Route path="/driver/earnings" element={
              <ProtectedRoute roles={['DRIVER']}><DriverEarningsPage /></ProtectedRoute>
            } />
            <Route path="/driver/profile" element={
              <ProtectedRoute roles={['DRIVER']}><DriverProfilePage /></ProtectedRoute>
            } />

            {/* Admin */}
            <Route path="/admin/dashboard" element={
              <ProtectedRoute roles={['ADMIN']}><AdminDashboard /></ProtectedRoute>
            } />
            <Route path="/admin/users" element={
              <ProtectedRoute roles={['ADMIN']}><AdminUsersPage /></ProtectedRoute>
            } />
            <Route path="/admin/drivers" element={
              <ProtectedRoute roles={['ADMIN']}><AdminDriversPage /></ProtectedRoute>
            } />
            <Route path="/admin/rides" element={
              <ProtectedRoute roles={['ADMIN']}><AdminRidesPage /></ProtectedRoute>
            } />
            <Route path="/admin/support" element={
              <ProtectedRoute roles={['ADMIN']}><AdminSupportPage /></ProtectedRoute>
            } />
            <Route path="/admin/promotions" element={
              <ProtectedRoute roles={['ADMIN']}><AdminPromotionsPage /></ProtectedRoute>
            } />
            <Route path="/admin/reviews" element={
              <ProtectedRoute roles={['ADMIN']}><AdminReviewsPage /></ProtectedRoute>
            } />
            <Route path="/admin/audit-logs" element={
              <ProtectedRoute roles={['ADMIN']}><AdminAuditLogsPage /></ProtectedRoute>
            } />

            {/* Notifications (all authenticated users) */}
            <Route path="/notifications" element={
              <ProtectedRoute roles={['RIDER', 'DRIVER', 'ADMIN']}><NotificationsPage /></ProtectedRoute>
            } />

            {/* Fallback */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Suspense>
      </main>
      <Footer />
    </div>
  );
}

function roleHome(role: string): string {
  if (role === 'RIDER')  return '/rider/dashboard';
  if (role === 'DRIVER') return '/driver/dashboard';
  if (role === 'ADMIN')  return '/admin/dashboard';
  return '/';
}
