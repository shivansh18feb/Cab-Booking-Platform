import React, { useState, useEffect } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { driverService } from '../../services/driverService';
import { Car, Star, CheckCircle2, AlertCircle, Save, Edit3, ArrowLeft } from 'lucide-react';

export default function DriverProfilePage() {
  const { user } = useAuth();
  const queryClient = useQueryClient();

  const { data: profile, isLoading } = useQuery({
    queryKey: ['driverProfile'],
    queryFn: driverService.getProfile,
  });

  const [isEditingVehicle, setIsEditingVehicle] = useState(false);
  const [make, setMake] = useState('');
  const [model, setModel] = useState('');
  const [year, setYear] = useState(2023);
  const [registrationNumber, setRegistrationNumber] = useState('');
  const [color, setColor] = useState('');
  const [category, setCategory] = useState('SEDAN');
  const [seatingCapacity, setSeatingCapacity] = useState(4);
  const [vehicleMsg, setVehicleMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [savingVehicle, setSavingVehicle] = useState(false);

  useEffect(() => {
    if (profile?.vehicle) {
      setMake(profile.vehicle.make || '');
      setModel(profile.vehicle.model || '');
      setYear(profile.vehicle.year || 2023);
      setRegistrationNumber(profile.vehicle.registrationNumber || '');
      setColor(profile.vehicle.color || '');
      setCategory(profile.vehicle.category || 'SEDAN');
      setSeatingCapacity(profile.vehicle.seatingCapacity || 4);
    }
  }, [profile]);

  const handleSaveVehicle = async (e: React.FormEvent) => {
    e.preventDefault();
    setSavingVehicle(true);
    setVehicleMsg(null);
    try {
      await driverService.updateVehicle({
        make,
        model,
        year: Number(year),
        registrationNumber,
        color,
        vehicleCategory: category,
        seatingCapacity: Number(seatingCapacity),
      });
      setVehicleMsg({ type: 'success', text: 'Vehicle details updated successfully.' });
      setIsEditingVehicle(false);
      queryClient.invalidateQueries({ queryKey: ['driverProfile'] });
    } catch (err: any) {
      setVehicleMsg({
        type: 'error',
        text: err.response?.data?.message || 'Failed to update vehicle details.',
      });
    } finally {
      setSavingVehicle(false);
    }
  };

  if (isLoading) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-12">
        <div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin mx-auto" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-3xl mx-auto space-y-6">
        {/* Header navigation */}
        <div className="flex items-center justify-between">
          <Link
            to="/driver/dashboard"
            className="inline-flex items-center text-xs font-semibold text-slate-500 hover:text-slate-800 transition"
          >
            <ArrowLeft className="w-4 h-4 mr-1" /> Back to Driver Dashboard
          </Link>
          <span
            className={`px-3 py-1 rounded-full text-xs font-bold border ${
              profile?.verificationStatus === 'APPROVED'
                ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                : 'bg-amber-50 text-amber-700 border-amber-200'
            }`}
          >
            {profile?.verificationStatus === 'APPROVED' ? 'Verified Driver' : 'Pending Verification'}
          </span>
        </div>

        {/* Profile Card */}
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 flex flex-col sm:flex-row items-center gap-6">
          <div className="w-20 h-20 bg-gradient-to-br from-indigo-600 to-indigo-800 rounded-2xl flex items-center justify-center text-white text-3xl font-extrabold shadow-sm shrink-0">
            {user?.fullName?.[0]?.toUpperCase() ?? 'D'}
          </div>
          <div className="text-center sm:text-left flex-1">
            <h1 className="text-2xl font-extrabold text-slate-900">{user?.fullName}</h1>
            <p className="text-xs text-slate-500 mt-0.5">{user?.email} • {user?.phoneNumber}</p>
            <div className="mt-3 flex flex-wrap gap-2 justify-center sm:justify-start">
              <span className="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-semibold bg-slate-100 text-slate-700">
                License: {profile?.licenseNumber || 'Verified'}
              </span>
              <span className="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-semibold bg-amber-50 text-amber-800 border border-amber-100">
                <Star className="w-3.5 h-3.5 mr-1 text-amber-500 fill-amber-500" />
                {profile?.averageRating ? profile.averageRating.toFixed(1) : '5.0'} Rating
              </span>
              <span className="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-semibold bg-indigo-50 text-indigo-700">
                {profile?.totalRides ?? 0} Completed Rides
              </span>
            </div>
          </div>
        </div>

        {/* Vehicle Management Card */}
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 sm:p-8">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center space-x-3">
              <div className="p-2.5 bg-indigo-50 text-indigo-600 rounded-xl">
                <Car className="w-5 h-5" />
              </div>
              <div>
                <h2 className="text-lg font-bold text-slate-900">Vehicle Settings</h2>
                <p className="text-xs text-slate-500">Manage your active car registration and category specifications.</p>
              </div>
            </div>
            {!isEditingVehicle && (
              <button
                type="button"
                onClick={() => setIsEditingVehicle(true)}
                className="inline-flex items-center px-3.5 py-1.5 rounded-xl text-xs font-semibold bg-slate-100 hover:bg-slate-200 text-slate-700 transition"
              >
                <Edit3 className="w-3.5 h-3.5 mr-1.5" /> Edit Vehicle
              </button>
            )}
          </div>

          {vehicleMsg && (
            <div
              className={`mb-6 p-4 rounded-xl flex items-center gap-3 text-sm font-medium ${
                vehicleMsg.type === 'success'
                  ? 'bg-emerald-50 text-emerald-800 border border-emerald-200'
                  : 'bg-rose-50 text-rose-800 border border-rose-200'
              }`}
            >
              {vehicleMsg.type === 'success' ? (
                <CheckCircle2 className="w-5 h-5 flex-shrink-0" />
              ) : (
                <AlertCircle className="w-5 h-5 flex-shrink-0" />
              )}
              <span>{vehicleMsg.text}</span>
            </div>
          )}

          {!isEditingVehicle ? (
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-5">
              <div className="p-4 bg-slate-50 rounded-xl border border-slate-100">
                <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">Make & Model</p>
                <p className="text-sm font-bold text-slate-900 mt-1">
                  {profile?.vehicle ? `${profile.vehicle.make} ${profile.vehicle.model}` : 'Not registered'}
                </p>
              </div>
              <div className="p-4 bg-slate-50 rounded-xl border border-slate-100">
                <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">Registration Plate</p>
                <p className="text-sm font-bold text-slate-900 mt-1 font-mono">
                  {profile?.vehicle?.registrationNumber || '—'}
                </p>
              </div>
              <div className="p-4 bg-slate-50 rounded-xl border border-slate-100">
                <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">Category</p>
                <p className="text-sm font-bold text-indigo-600 mt-1">
                  {profile?.vehicle?.category || 'SEDAN'}
                </p>
              </div>
              <div className="p-4 bg-slate-50 rounded-xl border border-slate-100">
                <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">Year</p>
                <p className="text-sm font-bold text-slate-900 mt-1">
                  {profile?.vehicle?.year || '—'}
                </p>
              </div>
              <div className="p-4 bg-slate-50 rounded-xl border border-slate-100">
                <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">Color</p>
                <p className="text-sm font-bold text-slate-900 mt-1">
                  {profile?.vehicle?.color || '—'}
                </p>
              </div>
              <div className="p-4 bg-slate-50 rounded-xl border border-slate-100">
                <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">Capacity</p>
                <p className="text-sm font-bold text-slate-900 mt-1">
                  {profile?.vehicle?.seatingCapacity || 4} Seats
                </p>
              </div>
            </div>
          ) : (
            <form onSubmit={handleSaveVehicle} className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">Make</label>
                  <input
                    type="text"
                    required
                    value={make}
                    onChange={(e) => setMake(e.target.value)}
                    placeholder="e.g. Toyota"
                    className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm focus:ring-2 focus:ring-indigo-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">Model</label>
                  <input
                    type="text"
                    required
                    value={model}
                    onChange={(e) => setModel(e.target.value)}
                    placeholder="e.g. Camry"
                    className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm focus:ring-2 focus:ring-indigo-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">Year</label>
                  <input
                    type="number"
                    required
                    min={2005}
                    max={2030}
                    value={year}
                    onChange={(e) => setYear(Number(e.target.value))}
                    className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm focus:ring-2 focus:ring-indigo-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">Registration Plate</label>
                  <input
                    type="text"
                    required
                    value={registrationNumber}
                    onChange={(e) => setRegistrationNumber(e.target.value.toUpperCase())}
                    placeholder="e.g. DL-01-AB-1234"
                    className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm uppercase font-mono focus:ring-2 focus:ring-indigo-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">Color</label>
                  <input
                    type="text"
                    required
                    value={color}
                    onChange={(e) => setColor(e.target.value)}
                    placeholder="e.g. Silver"
                    className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm focus:ring-2 focus:ring-indigo-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-1">Category</label>
                  <select
                    value={category}
                    onChange={(e) => setCategory(e.target.value)}
                    className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm focus:ring-2 focus:ring-indigo-500 bg-white"
                  >
                    <option value="HATCHBACK">Hatchback (Affordable & compact)</option>
                    <option value="SEDAN">Sedan (Comfort & style)</option>
                    <option value="SUV">SUV (Spacious 6-seater)</option>
                    <option value="PREMIUM">Premium (Luxury executive)</option>
                  </select>
                </div>
              </div>

              <div className="flex justify-end gap-3 pt-3">
                <button
                  type="button"
                  onClick={() => setIsEditingVehicle(false)}
                  className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-600 border border-slate-200 hover:bg-slate-50 transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={savingVehicle}
                  className="inline-flex items-center px-5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-semibold shadow-sm transition disabled:opacity-50"
                >
                  <Save className="w-4 h-4 mr-1.5" />
                  {savingVehicle ? 'Saving...' : 'Save Vehicle'}
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}
