import { useEffect, useRef, useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { driverService } from '../../services/driverService';
import { websocketService } from '../../services/websocketService';
import { useAuth } from '../../context/AuthContext';
import type { Ride } from '../../types/ride';

interface IncomingRequest {
  rideId: string;
  pickupAddress: string;
  dropoffAddress: string;
  estimatedFare: number;
  vehicleCategory: string;
}

const ACCEPT_TIMEOUT = 30; // seconds

export default function DriverDashboard() {
  const { user } = useAuth();
  const qc = useQueryClient();
  const [incomingRequest, setIncomingRequest] = useState<IncomingRequest | null>(null);
  const [countdown, setCountdown] = useState(ACCEPT_TIMEOUT);
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const { data: profile } = useQuery({
    queryKey: ['driverProfile'],
    queryFn: driverService.getProfile,
  });

  const { data: activeRide } = useQuery<Ride | null>({
    queryKey: ['driverActiveRide'],
    queryFn: async () => {
      try {
        const res = await driverService.getRequests();
        return res.find((r: Ride) => ['ACCEPTED', 'DRIVER_ARRIVING', 'IN_PROGRESS'].includes(r.status)) ?? null;
      } catch { return null; }
    },
    refetchInterval: 15_000,
  });

  const toggleMutation = useMutation({
    mutationFn: (online: boolean) => driverService.updateAvailability(online),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['driverProfile'] }),
  });

  const acceptMutation = useMutation({
    mutationFn: (rideId: string | number) => driverService.acceptRide(rideId),
    onSuccess: () => { setIncomingRequest(null); qc.invalidateQueries({ queryKey: ['driverActiveRide'] }); },
  });

  const updateStatusMutation = useMutation({
    mutationFn: ({ rideId, status }: { rideId: string | number; status: string }) =>
      driverService.updateRideStatus(rideId, status),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['driverActiveRide'] }),
  });

  /* WebSocket subscription */
  useEffect(() => {
    if (!user?.id) return;
    websocketService.connect(() => {
      websocketService.subscribe(`/topic/drivers/${user.id}/requests`, (req: IncomingRequest) => {
        setIncomingRequest(req);
        setCountdown(ACCEPT_TIMEOUT);
      });
    });
  }, [user?.id]);

  /* Countdown timer for incoming requests */
  useEffect(() => {
    if (!incomingRequest) { timerRef.current && clearInterval(timerRef.current); return; }
    timerRef.current = setInterval(() => {
      setCountdown(c => {
        if (c <= 1) { clearInterval(timerRef.current!); setIncomingRequest(null); return 0; }
        return c - 1;
      });
    }, 1000);
    return () => { timerRef.current && clearInterval(timerRef.current); };
  }, [incomingRequest]);

  const isOnline = profile?.availability === 'ONLINE';

  return (
    <div className="max-w-3xl mx-auto px-4 py-8 space-y-6 fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="page-title">Driver Dashboard</h1>
          <p className="text-gray-500 text-sm">Welcome, {user?.fullName?.split(' ')[0]}</p>
        </div>
        <Link to="/driver/earnings" className="btn-secondary text-sm">💰 Earnings</Link>
      </div>

      {/* Incoming Request — overlay-style */}
      {incomingRequest && (
        <div className="card border-2 border-yellow-400 bg-yellow-50 animate-pulse-once">
          <div className="flex items-center justify-between mb-3">
            <h2 className="font-bold text-yellow-800 text-lg">🚨 New Ride Request!</h2>
            <div className="flex items-center gap-2">
              <div className="w-10 h-10 rounded-full bg-yellow-200 flex items-center justify-center font-bold text-yellow-800">
                {countdown}
              </div>
              <span className="text-xs text-yellow-700">sec left</span>
            </div>
          </div>
          <div className="space-y-1 text-sm text-gray-800 mb-4">
            <p><span className="font-medium">Pickup:</span> {incomingRequest.pickupAddress}</p>
            <p><span className="font-medium">Drop-off:</span> {incomingRequest.dropoffAddress}</p>
            <p><span className="font-medium">Vehicle:</span> {incomingRequest.vehicleCategory}</p>
            <p><span className="font-medium">Fare:</span> ~\${incomingRequest.estimatedFare?.toFixed(2)}</p>
          </div>
          <div className="flex gap-3">
            <button
              onClick={() => setIncomingRequest(null)}
              className="btn-secondary flex-1"
            >
              Decline
            </button>
            <button
              onClick={() => acceptMutation.mutate(incomingRequest.rideId)}
              disabled={acceptMutation.isPending}
              className="btn-primary flex-1"
            >
              {acceptMutation.isPending ? 'Accepting…' : 'Accept Ride'}
            </button>
          </div>
        </div>
      )}

      {/* Availability Toggle */}
      <div className="card flex items-center justify-between">
        <div>
          <h2 className="section-title">Status</h2>
          <p className="text-sm text-gray-500 mt-0.5">
            {isOnline ? 'You are online and receiving requests' : 'Go online to start receiving rides'}
          </p>
        </div>
        <button
          onClick={() => toggleMutation.mutate(!isOnline)}
          disabled={toggleMutation.isPending}
          className={`relative inline-flex h-8 w-14 items-center rounded-full transition-colors focus:outline-none ${isOnline ? 'bg-green-500' : 'bg-gray-300'}`}
        >
          <span className={`inline-block w-6 h-6 transform rounded-full bg-white shadow transition-transform ${isOnline ? 'translate-x-7' : 'translate-x-1'}`} />
        </button>
      </div>

      {/* Active Trip Controls */}
      {activeRide && (
        <div className="card border-l-4 border-indigo-500">
          <div className="flex items-center gap-2 mb-3">
            <span className="live-pulse w-2 h-2 bg-indigo-500 rounded-full" />
            <h2 className="section-title">Active Trip</h2>
          </div>
          <div className="text-sm space-y-1 mb-4">
            <p><span className="font-medium">Pickup:</span> {activeRide.pickupAddress}</p>
            <p><span className="font-medium">Drop-off:</span> {activeRide.dropoffAddress}</p>
            <p><span className="font-medium">Status:</span> <span className="text-indigo-600 font-semibold">{activeRide.status.replace(/_/g, ' ')}</span></p>
          </div>
          <div className="flex flex-wrap gap-2">
            {activeRide.status === 'ACCEPTED' && (
              <button
                onClick={() => updateStatusMutation.mutate({ rideId: activeRide.id, status: 'DRIVER_ARRIVING' })}
                className="btn-primary text-sm"
              >
                🚗 Head to Pickup
              </button>
            )}
            {activeRide.status === 'DRIVER_ARRIVING' && (
              <button
                onClick={() => updateStatusMutation.mutate({ rideId: activeRide.id, status: 'DRIVER_ARRIVED' })}
                className="btn-primary text-sm"
              >
                📍 Arrived at Pickup
              </button>
            )}
            {activeRide.status === 'DRIVER_ARRIVED' && (
              <button
                onClick={() => updateStatusMutation.mutate({ rideId: activeRide.id, status: 'IN_PROGRESS' })}
                className="btn-primary text-sm"
              >
                ▶️ Start Ride
              </button>
            )}
            {activeRide.status === 'IN_PROGRESS' && (
              <button
                onClick={() => updateStatusMutation.mutate({ rideId: activeRide.id, status: 'COMPLETED' })}
                className="btn-primary text-sm bg-green-600 hover:bg-green-700"
              >
                ✅ Complete Ride
              </button>
            )}
          </div>
        </div>
      )}

      {/* Stats */}
      {profile && (
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
          {[
            { label: 'Rating',      value: profile.averageRating?.toFixed(1) ?? '—', icon: '⭐' },
            { label: 'Total Rides', value: profile.totalRides ?? 0,                   icon: '🚗' },
            { label: 'Verification',value: profile.verificationStatus,                icon: '🛡️' },
          ].map(s => (
            <div key={s.label} className="card text-center">
              <div className="text-2xl mb-1">{s.icon}</div>
              <p className="text-xl font-bold text-gray-900">{s.value}</p>
              <p className="text-xs text-gray-400 mt-0.5">{s.label}</p>
            </div>
          ))}
        </div>
      )}

      {/* Quick Links */}
      <div className="grid grid-cols-2 gap-4">
        <Link to="/driver/earnings" className="card-hover text-center hover:border-indigo-200">
          <div className="text-3xl mb-2">💰</div>
          <p className="text-sm font-medium text-gray-700">Earnings</p>
        </Link>
        <Link to="/driver/profile" className="card-hover text-center hover:border-indigo-200">
          <div className="text-3xl mb-2">👤</div>
          <p className="text-sm font-medium text-gray-700">Profile</p>
        </Link>
      </div>
    </div>
  );
}
