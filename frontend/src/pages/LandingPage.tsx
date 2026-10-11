import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Car,
  Compass,
  ShieldCheck,
  Zap,
  MapPin,
  Clock,
  ArrowRight,
  Star,
  DollarSign,
} from 'lucide-react';
import { RideMap } from '../components/Map/RideMap';
import { rideService } from '../services/rideService';
import { FareEstimate, VehicleCategory } from '../types/ride';

export const LandingPage: React.FC = () => {
  const navigate = useNavigate();

  // Interactive Fare Preview on Landing Page
  const [pickupAddress, setPickupAddress] = useState('Times Square, New York, NY');
  const [dropoffAddress, setDropoffAddress] = useState('Central Park Zoo, New York, NY');
  const [vehicleCategory, setVehicleCategory] = useState<VehicleCategory>('SEDAN');
  const [estimate, setEstimate] = useState<FareEstimate | null>(null);
  const [isEstimating, setIsEstimating] = useState(false);

  // Preset demo coordinates for realistic preview
  const pickupCoords = { lat: 40.7580, lng: -73.9855, label: 'Times Square' };
  const dropoffCoords = { lat: 40.7678, lng: -73.9718, label: 'Central Park' };

  const handleCalculatePreview = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsEstimating(true);
    try {
      const res = await rideService.getEstimate({
        pickupLatitude: pickupCoords.lat,
        pickupLongitude: pickupCoords.lng,
        dropoffLatitude: dropoffCoords.lat,
        dropoffLongitude: dropoffCoords.lng,
        vehicleCategory,
      });
      setEstimate(res);
    } catch {
      // Fallback estimate preview if backend is not yet started
      setEstimate({
        vehicleCategory,
        distanceKm: 2.8,
        estimatedDurationMinutes: 11,
        baseFare: 3.50,
        distanceCharge: 4.20,
        timeCharge: 3.85,
        serviceFee: 1.50,
        surgeMultiplier: 1.0,
        discountAmount: 0.0,
        totalEstimatedFare: 13.05,
        currency: 'USD',
      });
    } finally {
      setIsEstimating(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-900 text-white selection:bg-emerald-500 selection:text-white">
      {/* Hero Section */}
      <section className="relative pt-12 pb-24 overflow-hidden">
        <div className="absolute inset-0 bg-radial-gradient from-emerald-500/10 via-transparent to-transparent pointer-events-none" />

        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
            {/* Hero Left Content */}
            <div className="lg:col-span-7 space-y-8">
              <div className="inline-flex items-center space-x-2 bg-emerald-500/10 border border-emerald-500/30 px-3.5 py-1.5 rounded-full text-emerald-400 text-xs font-semibold uppercase tracking-wider">
                <Zap className="h-4 w-4" />
                <span>Next-Generation Urban Ride-Hailing</span>
              </div>

              <h1 className="text-4xl sm:text-6xl font-extrabold tracking-tight leading-tight">
                Anywhere in the city, <br />
                <span className="bg-gradient-to-r from-emerald-400 to-teal-300 bg-clip-text text-transparent">
                  reliably and safely.
                </span>
              </h1>

              <p className="text-lg text-slate-300 max-w-xl leading-relaxed">
                Connect with verified local drivers in seconds. Transparent upfront pricing, zero hidden fees, live GPS tracking, and seamless Stripe payments.
              </p>

              <div className="flex flex-col sm:flex-row items-stretch sm:items-center space-y-3 sm:space-y-0 sm:space-x-4">
                <Link
                  to="/rider/dashboard"
                  className="inline-flex items-center justify-center space-x-2 bg-emerald-500 hover:bg-emerald-600 text-white font-semibold px-6 py-3.5 rounded-xl shadow-lg shadow-emerald-500/25 transition text-base"
                >
                  <Compass className="h-5 w-5" />
                  <span>Book Ride Now</span>
                  <ArrowRight className="h-4 w-4 ml-1" />
                </Link>

                <Link
                  to="/driver/register"
                  className="inline-flex items-center justify-center space-x-2 bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 font-semibold px-6 py-3.5 rounded-xl transition text-base"
                >
                  <Car className="h-5 w-5 text-emerald-400" />
                  <span>Become a Driver</span>
                </Link>
              </div>

              {/* Social Proof Stats */}
              <div className="grid grid-cols-3 gap-6 pt-6 border-t border-slate-800">
                <div>
                  <p className="text-2xl sm:text-3xl font-extrabold text-white">4.9/5</p>
                  <p className="text-xs text-slate-400 flex items-center space-x-1 mt-0.5">
                    <Star className="h-3 w-3 fill-amber-400 text-amber-400 inline" />
                    <span>User Rating</span>
                  </p>
                </div>
                <div>
                  <p className="text-2xl sm:text-3xl font-extrabold text-white">&lt; 3 min</p>
                  <p className="text-xs text-slate-400 mt-0.5">Average Pickup ETA</p>
                </div>
                <div>
                  <p className="text-2xl sm:text-3xl font-extrabold text-white">100%</p>
                  <p className="text-xs text-slate-400 mt-0.5">Inspected Vehicles</p>
                </div>
              </div>
            </div>

            {/* Hero Right: Interactive Live Estimator & Map Card */}
            <div className="lg:col-span-5 bg-slate-800/90 border border-slate-700/80 rounded-3xl p-6 shadow-2xl backdrop-blur-sm space-y-5">
              <div className="flex items-center justify-between pb-3 border-b border-slate-700">
                <div className="flex items-center space-x-2">
                  <div className="p-2 rounded-lg bg-emerald-500/20 text-emerald-400">
                    <DollarSign className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="font-bold text-base text-white">Upfront Fare Calculator</h3>
                    <p className="text-xs text-slate-400">Transparent pricing before booking</p>
                  </div>
                </div>
                <span className="text-xs px-2.5 py-1 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
                  Live Engine
                </span>
              </div>

              {/* Mini Map Preview */}
              <RideMap
                pickup={pickupCoords}
                dropoff={dropoffCoords}
                heightClass="h-[180px]"
              />

              {/* Fare Form */}
              <form onSubmit={handleCalculatePreview} className="space-y-4">
                <div className="space-y-2">
                  <div className="flex items-center space-x-2 bg-slate-900 border border-slate-700 px-3 py-2 rounded-xl text-sm">
                    <div className="h-3 w-3 rounded-full bg-emerald-400 flex-shrink-0" />
                    <input
                      type="text"
                      value={pickupAddress}
                      onChange={(e) => setPickupAddress(e.target.value)}
                      placeholder="Pickup address"
                      className="bg-transparent text-white w-full focus:outline-none text-xs"
                    />
                  </div>

                  <div className="flex items-center space-x-2 bg-slate-900 border border-slate-700 px-3 py-2 rounded-xl text-sm">
                    <div className="h-3 w-3 rounded-full bg-rose-400 flex-shrink-0" />
                    <input
                      type="text"
                      value={dropoffAddress}
                      onChange={(e) => setDropoffAddress(e.target.value)}
                      placeholder="Drop-off address"
                      className="bg-transparent text-white w-full focus:outline-none text-xs"
                    />
                  </div>
                </div>

                {/* Vehicle Selector */}
                <div className="grid grid-cols-4 gap-2">
                  {(['SEDAN', 'HATCHBACK', 'SUV', 'PREMIUM'] as VehicleCategory[]).map((cat) => (
                    <button
                      type="button"
                      key={cat}
                      onClick={() => setVehicleCategory(cat)}
                      className={`py-2 text-[11px] font-semibold rounded-lg border transition ${
                        vehicleCategory === cat
                          ? 'bg-emerald-500 border-emerald-400 text-white shadow-md'
                          : 'bg-slate-900/60 border-slate-700 text-slate-300 hover:border-slate-600'
                      }`}
                    >
                      {cat}
                    </button>
                  ))}
                </div>

                <button
                  type="submit"
                  disabled={isEstimating}
                  className="w-full bg-emerald-500 hover:bg-emerald-600 text-white py-3 rounded-xl font-bold text-sm transition shadow-lg shadow-emerald-500/20 disabled:opacity-50"
                >
                  {isEstimating ? 'Calculating Fare...' : 'Calculate Upfront Fare'}
                </button>
              </form>

              {/* Estimate Result Breakdown */}
              {estimate && (
                <div className="bg-slate-900/90 border border-emerald-500/30 rounded-2xl p-4 space-y-2 animate-fadeIn">
                  <div className="flex justify-between items-center">
                    <span className="text-xs text-slate-400">Estimated Trip Fare</span>
                    <span className="text-xl font-black text-emerald-400">
                      ${estimate.totalEstimatedFare.toFixed(2)}
                    </span>
                  </div>
                  <div className="flex justify-between text-[11px] text-slate-400 pt-1 border-t border-slate-800">
                    <span>Distance: {estimate.distanceKm} km</span>
                    <span>Travel Time: ~{estimate.estimatedDurationMinutes} mins</span>
                    <span>Category: {estimate.vehicleCategory}</span>
                  </div>
                  <button
                    onClick={() => navigate('/rider/dashboard')}
                    className="w-full mt-2 bg-slate-800 hover:bg-slate-700 text-emerald-400 py-2 rounded-lg text-xs font-bold border border-emerald-500/30 transition flex items-center justify-center space-x-1"
                  >
                    <span>Proceed to Book This Trip</span>
                    <ArrowRight className="h-3 w-3" />
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </section>

      {/* Feature Pillars */}
      <section className="py-20 bg-slate-950 border-t border-slate-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-2xl mx-auto space-y-3 mb-16">
            <h2 className="text-3xl font-extrabold text-white">Why Riders Choose CabFlow</h2>
            <p className="text-slate-400 text-sm leading-relaxed">
              Designed for passenger peace of mind and driver prosperity with verified standards.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="bg-slate-900 border border-slate-800 p-8 rounded-3xl space-y-4 hover:border-emerald-500/50 transition">
              <div className="h-12 w-12 rounded-2xl bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-center text-emerald-400">
                <ShieldCheck className="h-6 w-6" />
              </div>
              <h3 className="text-xl font-bold text-white">Verified Drivers & Cars</h3>
              <p className="text-sm text-slate-400 leading-relaxed">
                Every driver undergoes license verification, identity authentication, and vehicle safety inspections before accepting rides.
              </p>
            </div>

            <div className="bg-slate-900 border border-slate-800 p-8 rounded-3xl space-y-4 hover:border-emerald-500/50 transition">
              <div className="h-12 w-12 rounded-2xl bg-teal-500/10 border border-teal-500/30 flex items-center justify-center text-teal-400">
                <MapPin className="h-6 w-6" />
              </div>
              <h3 className="text-xl font-bold text-white">Live Real-Time GPS</h3>
              <p className="text-sm text-slate-400 leading-relaxed">
                Track your assigned vehicle in real-time over WebSocket STOMP channels from dispatch through pickup and dropoff.
              </p>
            </div>

            <div className="bg-slate-900 border border-slate-800 p-8 rounded-3xl space-y-4 hover:border-emerald-500/50 transition">
              <div className="h-12 w-12 rounded-2xl bg-sky-500/10 border border-sky-500/30 flex items-center justify-center text-sky-400">
                <Clock className="h-6 w-6" />
              </div>
              <h3 className="text-xl font-bold text-white">Guaranteed Zero Surge Spikes</h3>
              <p className="text-sm text-slate-400 leading-relaxed">
                Fair, transparent fares calculated from base rates and real distance. No predatory price spikes during emergencies.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* How it works Section */}
      <section className="py-20 bg-slate-900">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-xl mx-auto space-y-3 mb-16">
            <h2 className="text-3xl font-extrabold text-white">How CabFlow Works</h2>
            <p className="text-slate-400 text-sm">Getting from A to B has never been this seamless.</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            {[
              { step: '01', title: 'Pick Your Destination', desc: 'Select pickup and dropoff addresses or tap on our interactive map.' },
              { step: '02', title: 'Select Vehicle Category', desc: 'Choose Sedan, Hatchback, SUV, or Premium with transparent upfront fare.' },
              { step: '03', title: 'Real-Time Match', desc: 'Our concurrency-safe dispatch assigns the closest verified online driver.' },
              { step: '04', title: 'Ride & Pay Digitally', desc: 'Enjoy your trip, confirm payment securely via Stripe, and download your invoice.' },
            ].map((item, idx) => (
              <div key={idx} className="space-y-3 relative">
                <div className="text-4xl font-black text-emerald-500/30">{item.step}</div>
                <h4 className="text-lg font-bold text-white">{item.title}</h4>
                <p className="text-sm text-slate-400 leading-relaxed">{item.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
};

export default LandingPage;
