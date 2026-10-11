import { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { rideService } from '../../services/rideService';
import RideMap from '../../components/Map/RideMap';
import { VehicleCategory } from '../../types/ride';
import type { LocationPoint, FareEstimate } from '../../types/ride';
import { formatCurrency } from '../../utils/formatters';

const vehicleOptions: { type: VehicleCategory; label: string; icon: string; desc: string }[] = [
  { type: VehicleCategory.HATCHBACK, label: 'Hatchback', icon: '🚗', desc: 'Affordable & compact' },
  { type: VehicleCategory.SEDAN,     label: 'Sedan',     icon: '🚘', desc: 'Comfort & style' },
  { type: VehicleCategory.SUV,       label: 'SUV',       icon: '🚙', desc: 'Space for groups' },
  { type: VehicleCategory.PREMIUM,   label: 'Premium',   icon: '🏎️', desc: 'Luxury experience' },
];

type Step = 'map' | 'vehicle' | 'promo' | 'confirm';

export default function BookRidePage() {
  const navigate = useNavigate();

  const [pickup, setPickup]   = useState<LocationPoint | null>(null);
  const [dropoff, setDropoff] = useState<LocationPoint | null>(null);
  const [pickupAddr, setPickupAddr]   = useState('');
  const [dropoffAddr, setDropoffAddr] = useState('');
  const [vehicle, setVehicle]   = useState<VehicleCategory>(VehicleCategory.SEDAN);
  const [promoCode, setPromoCode] = useState('');
  const [estimate, setEstimate]   = useState<FareEstimate | null>(null);
  const [step, setStep] = useState<Step>('map');
  const [selectingPoint, setSelectingPoint] = useState<'pickup' | 'dropoff'>('pickup');
  const [estimateError, setEstimateError] = useState('');

  /* reverse-geocode via Nominatim */
  const reverseGeocode = useCallback(async (lat: number, lng: number): Promise<string> => {
    try {
      const res = await fetch(
        `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`
      );
      const json = await res.json();
      return json.display_name ?? `${lat.toFixed(5)}, ${lng.toFixed(5)}`;
    } catch {
      return `${lat.toFixed(5)}, ${lng.toFixed(5)}`;
    }
  }, []);

  const handleMapClick = useCallback(async (lat: number, lng: number) => {
    const addr = await reverseGeocode(lat, lng);
    if (selectingPoint === 'pickup') {
      setPickup({ lat, lng });
      setPickupAddr(addr);
      setSelectingPoint('dropoff');
    } else {
      setDropoff({ lat, lng });
      setDropoffAddr(addr);
    }
  }, [selectingPoint, reverseGeocode]);

  const estimateMutation = useMutation({
    mutationFn: () => rideService.getEstimate({
      pickupLat: pickup!.lat, pickupLng: pickup!.lng,
      dropoffLat: dropoff!.lat, dropoffLng: dropoff!.lng,
      vehicleCategory: vehicle,
      promoCode: promoCode || undefined,
    }),
    onSuccess: (data) => {
      setEstimate(data);
      setStep('confirm');
      setEstimateError('');
    },
    onError: (err: any) => {
      setEstimateError(err.response?.data?.message ?? 'Could not get estimate. Try again.');
    },
  });

  const bookMutation = useMutation({
    mutationFn: () => rideService.createRide({
      pickupLat: pickup!.lat, pickupLng: pickup!.lng, pickupAddress: pickupAddr,
      dropoffLat: dropoff!.lat, dropoffLng: dropoff!.lng, dropoffAddress: dropoffAddr,
      vehicleCategory: vehicle,
      promoCode: promoCode || undefined,
    }),
    onSuccess: (ride) => navigate(`/rider/ride/${ride.id}/track`),
  });

  const canEstimate = pickup && dropoff;

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-6xl mx-auto px-4 py-6">
        <h1 className="page-title mb-6">Book a Ride</h1>

        <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
          {/* Left Panel */}
          <div className="lg:col-span-2 space-y-4">

            {/* Step: Map Selection */}
            <div className="card">
              <h2 className="section-title mb-3">Set Locations</h2>

              {/* Pickup */}
              <div className="mb-3">
                <div className="flex items-center gap-2 mb-1">
                  <span className="w-6 h-6 bg-green-500 text-white rounded-full flex items-center justify-center text-xs font-bold">P</span>
                  <label className="text-sm font-medium text-gray-700">Pickup</label>
                </div>
                <div className="flex gap-2">
                  <input
                    value={pickupAddr}
                    onChange={e => setPickupAddr(e.target.value)}
                    placeholder="Click map or type address…"
                    className="input-field text-sm flex-1"
                  />
                  <button
                    type="button"
                    onClick={() => setSelectingPoint('pickup')}
                    className={`px-3 py-2 rounded-lg text-xs font-medium border transition-colors ${
                      selectingPoint === 'pickup' ? 'bg-green-500 text-white border-green-500' : 'border-gray-300 text-gray-600 hover:border-green-400'
                    }`}
                  >
                    📍
                  </button>
                </div>
              </div>

              {/* Dropoff */}
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <span className="w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center text-xs font-bold">D</span>
                  <label className="text-sm font-medium text-gray-700">Drop-off</label>
                </div>
                <div className="flex gap-2">
                  <input
                    value={dropoffAddr}
                    onChange={e => setDropoffAddr(e.target.value)}
                    placeholder="Click map or type address…"
                    className="input-field text-sm flex-1"
                  />
                  <button
                    type="button"
                    onClick={() => setSelectingPoint('dropoff')}
                    className={`px-3 py-2 rounded-lg text-xs font-medium border transition-colors ${
                      selectingPoint === 'dropoff' ? 'bg-red-500 text-white border-red-500' : 'border-gray-300 text-gray-600 hover:border-red-400'
                    }`}
                  >
                    🏁
                  </button>
                </div>
              </div>

              <p className="text-xs text-gray-400 mt-2">
                Selecting: <strong className={selectingPoint === 'pickup' ? 'text-green-600' : 'text-red-600'}>
                  {selectingPoint === 'pickup' ? 'Pickup point' : 'Drop-off point'}
                </strong> — click on the map
              </p>
            </div>

            {/* Vehicle Selector */}
            <div className="card">
              <h2 className="section-title mb-3">Choose Vehicle</h2>
              <div className="grid grid-cols-2 gap-2">
                {vehicleOptions.map(v => (
                  <button
                    key={v.type}
                    type="button"
                    onClick={() => setVehicle(v.type)}
                    className={`p-3 rounded-xl border-2 text-left transition-all ${
                      vehicle === v.type
                        ? 'border-indigo-500 bg-indigo-50'
                        : 'border-gray-200 hover:border-indigo-200'
                    }`}
                  >
                    <div className="text-2xl mb-1">{v.icon}</div>
                    <div className="text-sm font-semibold text-gray-900">{v.label}</div>
                    <div className="text-xs text-gray-500">{v.desc}</div>
                  </button>
                ))}
              </div>
            </div>

            {/* Promo */}
            <div className="card">
              <h2 className="section-title mb-3">Promo Code</h2>
              <div className="flex gap-2">
                <input
                  value={promoCode}
                  onChange={e => setPromoCode(e.target.value.toUpperCase())}
                  placeholder="WELCOME20"
                  className="input-field flex-1 uppercase"
                />
              </div>
            </div>

            {/* Estimate / Confirm */}
            {estimateError && (
              <div className="bg-red-50 border border-red-200 rounded-lg p-3 text-sm text-red-700">{estimateError}</div>
            )}

            {step !== 'confirm' ? (
              <button
                disabled={!canEstimate || estimateMutation.isPending}
                onClick={() => estimateMutation.mutate()}
                className="btn-primary w-full py-3 text-base"
              >
                {estimateMutation.isPending ? (
                  <span className="flex items-center justify-center gap-2">
                    <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                    Getting Estimate…
                  </span>
                ) : 'Get Fare Estimate'}
              </button>
            ) : estimate && (
              <div className="card border border-indigo-200 bg-indigo-50 space-y-3">
                <h3 className="font-semibold text-indigo-800">Fare Estimate</h3>
                <div className="text-sm space-y-1">
                  <div className="flex justify-between"><span className="text-gray-600">Distance</span><span className="font-medium">{estimate.distanceKm.toFixed(1)} km</span></div>
                  <div className="flex justify-between"><span className="text-gray-600">Duration</span><span className="font-medium">{estimate.durationMin} min</span></div>
                  <div className="flex justify-between"><span className="text-gray-600">Base Fare</span><span className="font-medium">{formatCurrency(estimate.baseFare, estimate.currency)}</span></div>
                  {estimate.discountAmount > 0 && (
                    <div className="flex justify-between text-green-600"><span>Promo Discount</span><span>-{formatCurrency(estimate.discountAmount, estimate.currency)}</span></div>
                  )}
                  <div className="flex justify-between font-bold text-indigo-900 text-base pt-1 border-t border-indigo-200">
                    <span>Total</span><span>{formatCurrency(estimate.estimatedFare ?? estimate.totalEstimatedFare ?? 0, estimate.currency)}</span>
                  </div>
                </div>
                <div className="flex gap-2">
                  <button onClick={() => setStep('map')} className="btn-secondary flex-1 text-sm">Change</button>
                  <button
                    onClick={() => bookMutation.mutate()}
                    disabled={bookMutation.isPending}
                    className="btn-primary flex-1 text-sm"
                  >
                    {bookMutation.isPending ? 'Booking…' : 'Confirm Booking'}
                  </button>
                </div>
                {bookMutation.isError && (
                  <p className="text-xs text-red-600">{(bookMutation.error as any)?.response?.data?.message ?? 'Booking failed'}</p>
                )}
              </div>
            )}
          </div>

          {/* Map */}
          <div className="lg:col-span-3">
            <div className="card p-0 overflow-hidden h-[500px] lg:h-[650px]">
              <RideMap
                pickup={pickup ?? undefined}
                dropoff={dropoff ?? undefined}
                onMapClick={handleMapClick}
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
