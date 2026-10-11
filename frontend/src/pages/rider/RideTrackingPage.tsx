import { useEffect, useRef, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { rideService } from '../../services/rideService';
import { websocketService } from '../../services/websocketService';
import RideMap from '../../components/Map/RideMap';
import type { LocationPoint } from '../../types/ride';

const STATUS_LABELS: Record<string, { label: string; color: string; icon: string }> = {
  REQUESTED:       { label: 'Finding a driver…',         color: 'text-yellow-600', icon: '🔍' },
  ACCEPTED:        { label: 'Driver accepted your ride',  color: 'text-blue-600',   icon: '✅' },
  DRIVER_ARRIVING: { label: 'Driver is on the way',       color: 'text-blue-600',   icon: '🚗' },
  IN_PROGRESS:     { label: 'Ride in progress',           color: 'text-indigo-600', icon: '🛣️' },
  COMPLETED:       { label: 'Ride completed',             color: 'text-green-600',  icon: '🎉' },
  CANCELLED:       { label: 'Ride cancelled',             color: 'text-red-600',    icon: '❌' },
};

export default function RideTrackingPage() {
  const { id } = useParams<{ id: string }>();
  const qc = useQueryClient();
  const [driverLocation, setDriverLocation] = useState<LocationPoint | null>(null);
  const subRef = useRef<(() => void) | null>(null);

  const { data: ride } = useQuery({
    queryKey: ['ride', id],
    queryFn: () => rideService.getRide(id!),
    refetchInterval: (query) => {
      const status = query.state.data?.status;
      return status === 'COMPLETED' || status === 'CANCELLED' ? false : 8000;
    },
  });

  const cancelMutation = useMutation({
    mutationFn: () => rideService.cancelRide(id!, 'Rider cancelled'),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['ride', id] }),
  });

  const rateMutation = useMutation({
    mutationFn: (rating: number) => rideService.submitRating(id!, rating, ''),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['ride', id] }),
  });

  const [ratingValue, setRatingValue] = useState(0);
  const [sosSuccess, setSosSuccess] = useState(false);

  const sosMutation = useMutation({
    mutationFn: () => rideService.triggerSos(id!, 'I need help!'),
    onSuccess: () => setSosSuccess(true),
  });

  /* WebSocket subscription */
  useEffect(() => {
    if (!id) return;

    websocketService.connect(() => {
      // Ride status updates
      subRef.current = websocketService.subscribe(`/topic/rides/${id}`, (msg) => {
        qc.setQueryData(['ride', id], (old: any) => ({ ...old, ...msg }));
      });

      // Driver location updates
      websocketService.subscribe(`/topic/rides/${id}/location`, (loc: any) => {
        setDriverLocation({ lat: loc.lat, lng: loc.lng });
      });
    });

    return () => {
      subRef.current?.();
      // Don't disconnect – other pages may be using it
    };
  }, [id, qc]);

  const statusInfo = STATUS_LABELS[ride?.status ?? ''] ?? { label: ride?.status ?? '', color: 'text-gray-600', icon: '🚕' };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-5xl mx-auto px-4 py-6">
        <div className="flex items-center gap-3 mb-6">
          <Link to="/rider/dashboard" className="text-gray-400 hover:text-gray-600">
            ← Back
          </Link>
          <h1 className="page-title">Live Tracking</h1>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
          {/* Info Panel */}
          <div className="lg:col-span-2 space-y-4">
            {/* Status */}
            <div className="card">
              <div className="flex items-center gap-3 mb-4">
                <span className="text-3xl">{statusInfo.icon}</span>
                <div>
                  <p className={`font-semibold ${statusInfo.color}`}>{statusInfo.label}</p>
                  <p className="text-xs text-gray-400">Ride #{id?.slice(0, 8)}</p>
                </div>
                {ride?.status === 'REQUESTED' && (
                  <span className="ml-auto live-pulse w-2 h-2 bg-yellow-400 rounded-full" />
                )}
              </div>

              {ride && (
                <div className="space-y-2 text-sm">
                  <div className="flex items-start gap-2">
                    <span className="w-5 h-5 bg-green-500 text-white rounded-full flex items-center justify-center text-xs shrink-0 mt-0.5">P</span>
                    <span className="text-gray-700">{ride.pickupAddress}</span>
                  </div>
                  <div className="flex items-start gap-2">
                    <span className="w-5 h-5 bg-red-500 text-white rounded-full flex items-center justify-center text-xs shrink-0 mt-0.5">D</span>
                    <span className="text-gray-700">{ride.dropoffAddress}</span>
                  </div>
                </div>
              )}
            </div>

            {/* Driver Info */}
            {ride?.driverId && (
              <div className="card">
                <h3 className="font-semibold text-gray-800 mb-3">Your Driver</h3>
                <div className="flex items-center gap-3">
                  <div className="w-12 h-12 bg-indigo-100 rounded-full flex items-center justify-center text-2xl">🧑‍✈️</div>
                  <div>
                    <p className="font-medium text-gray-900">{ride.driverName ?? 'Driver'}</p>
                    <p className="text-sm text-gray-500">{ride.vehicleCategory ?? 'Vehicle'}</p>
                  </div>
                  {ride.driverRating && (
                    <div className="ml-auto flex items-center gap-1">
                      <span className="text-yellow-500">⭐</span>
                      <span className="font-medium text-sm">{ride.driverRating.toFixed(1)}</span>
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Fare */}
            {ride && (
              <div className="card">
                <h3 className="font-semibold text-gray-800 mb-2">Fare</h3>
                <div className="text-2xl font-bold text-indigo-600">
                  \${(ride.finalFare ?? ride.estimatedFare ?? 0).toFixed(2)}
                </div>
                <p className="text-xs text-gray-400 mt-1">
                  {ride.status === 'COMPLETED' ? 'Final fare' : 'Estimated fare'}
                </p>
              </div>
            )}

            {/* Cancel */}
            {ride && ['REQUESTED', 'ACCEPTED'].includes(ride.status) && (
              <button
                onClick={() => cancelMutation.mutate()}
                disabled={cancelMutation.isPending}
                className="btn-danger w-full"
              >
                {cancelMutation.isPending ? 'Cancelling…' : 'Cancel Ride'}
              </button>
            )}

            {/* SOS Emergency Alert */}
            {ride && ['ACCEPTED', 'DRIVER_ARRIVING', 'DRIVER_ARRIVED', 'IN_PROGRESS'].includes(ride.status) && (
              <div className="card border-red-200 bg-red-50">
                <h3 className="font-semibold text-red-700 mb-2 flex items-center gap-2">
                  🆘 Emergency
                </h3>
                {sosSuccess ? (
                  <div className="text-sm text-green-700 font-medium text-center py-2">
                    ✅ SOS alert sent! Help is on the way.
                  </div>
                ) : (
                  <>
                    <p className="text-xs text-red-600 mb-3">
                      If you feel unsafe, tap the button below to alert emergency services and our safety team.
                    </p>
                    <button
                      onClick={() => sosMutation.mutate()}
                      disabled={sosMutation.isPending}
                      className="w-full py-2.5 bg-red-600 hover:bg-red-700 text-white font-bold rounded-lg transition-colors disabled:opacity-50"
                    >
                      {sosMutation.isPending ? 'Sending SOS…' : '🆘 Send SOS Alert'}
                    </button>
                  </>
                )}
              </div>
            )}

            {/* Rating */}
            {ride?.status === 'COMPLETED' && !ride.rated && (
              <div className="card">
                <h3 className="font-semibold text-gray-800 mb-3">Rate your ride</h3>
                <div className="flex gap-2 mb-3">
                  {[1, 2, 3, 4, 5].map(s => (
                    <button
                      key={s}
                      onClick={() => setRatingValue(s)}
                      className={`text-2xl transition-transform hover:scale-110 ${s <= ratingValue ? 'text-yellow-400' : 'text-gray-300'}`}
                    >
                      ★
                    </button>
                  ))}
                </div>
                <button
                  disabled={ratingValue === 0 || rateMutation.isPending}
                  onClick={() => rateMutation.mutate(ratingValue)}
                  className="btn-primary w-full text-sm"
                >
                  {rateMutation.isPending ? 'Submitting…' : 'Submit Rating'}
                </button>
              </div>
            )}

            {ride?.status === 'COMPLETED' && (
              <Link to={`/rider/ride/${id}`} className="btn-secondary w-full text-center block">
                View Receipt
              </Link>
            )}
          </div>

          {/* Map */}
          <div className="lg:col-span-3">
            <div className="card p-0 overflow-hidden h-[480px] lg:h-[600px]">
              {ride && (
                <RideMap
                  pickup={ride.pickupLat != null && ride.pickupLng != null ? { lat: ride.pickupLat, lng: ride.pickupLng } : undefined}
                  dropoff={ride.dropoffLat != null && ride.dropoffLng != null ? { lat: ride.dropoffLat, lng: ride.dropoffLng } : undefined}
                  driverLocation={driverLocation ?? undefined}
                />
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
