import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { authService } from '../services/authService';

const schema = z.object({
  fullName: z.string().min(2, 'Full name required'),
  email: z.string().email('Valid email required'),
  phone: z.string().min(10, 'Valid phone number required'),
  password: z.string().min(8, 'At least 8 characters').regex(/[A-Z]/, 'Must contain uppercase').regex(/[0-9]/, 'Must contain a digit'),
  licenseNumber: z.string().min(4, 'License number required'),
  vehicleType: z.enum(['SEDAN', 'HATCHBACK', 'SUV', 'PREMIUM'], { required_error: 'Select vehicle type' }),
  make: z.string().min(2, 'Vehicle make required'),
  model: z.string().min(1, 'Vehicle model required'),
  year: z.coerce.number().min(2000).max(new Date().getFullYear() + 1),
  registrationNumber: z.string().min(4, 'Registration number required'),
  color: z.string().min(2, 'Color required'),
  seatingCapacity: z.coerce.number().min(2).max(12),
});

type FormData = z.infer<typeof schema>;

const vehicleTypes = [
  { value: 'HATCHBACK', label: 'Hatchback' },
  { value: 'SEDAN',     label: 'Sedan' },
  { value: 'SUV',       label: 'SUV' },
  { value: 'PREMIUM',   label: 'Premium' },
];

export default function DriverRegisterPage() {
  const navigate = useNavigate();
  const [serverError, setServerError] = useState('');
  const [success, setSuccess] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({ resolver: zodResolver(schema) });

  const onSubmit = async (data: FormData) => {
    setServerError('');
    try {
      await authService.registerDriver({
        fullName: data.fullName,
        email: data.email,
        phoneNumber: data.phone,
        password: data.password,
        licenseNumber: data.licenseNumber,
        vehicleType: data.vehicleType,
        make: data.make,
        model: data.model,
        year: data.year,
        registrationNumber: data.registrationNumber,
        color: data.color,
        seatingCapacity: data.seatingCapacity,
      });
      setSuccess(true);
      setTimeout(() => navigate('/login'), 2500);
    } catch (err: any) {
      setServerError(err.response?.data?.message ?? 'Registration failed. Please try again.');
    }
  };

  if (success) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-50 to-purple-50 p-4">
        <div className="card max-w-md w-full text-center fade-in">
          <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h2 className="text-xl font-bold text-gray-900 mb-2">Application Submitted!</h2>
          <p className="text-gray-600 mb-1">Your driver application is under review.</p>
          <p className="text-sm text-gray-400">Redirecting to login…</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 to-purple-50 py-10 px-4">
      <div className="max-w-2xl mx-auto">
        {/* Header */}
        <div className="text-center mb-8">
          <Link to="/" className="inline-flex items-center gap-2 text-2xl font-bold text-indigo-600 mb-4">
            <span className="text-3xl">🚖</span> CabFlow
          </Link>
          <h1 className="text-3xl font-bold text-gray-900">Become a Driver</h1>
          <p className="text-gray-500 mt-2">Earn on your schedule. Join thousands of CabFlow drivers.</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          {/* Personal Info */}
          <div className="card">
            <h2 className="section-title mb-4 flex items-center gap-2">
              <span className="w-7 h-7 bg-indigo-100 text-indigo-600 rounded-full flex items-center justify-center text-sm font-bold">1</span>
              Personal Information
            </h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="label-field">Full Name</label>
                <input {...register('fullName')} className={`input-field ${errors.fullName ? 'input-error' : ''}`} placeholder="Jane Smith" />
                {errors.fullName && <p className="text-xs text-red-500 mt-1">{errors.fullName.message}</p>}
              </div>
              <div>
                <label className="label-field">Email Address</label>
                <input {...register('email')} type="email" className={`input-field ${errors.email ? 'input-error' : ''}`} placeholder="jane@example.com" />
                {errors.email && <p className="text-xs text-red-500 mt-1">{errors.email.message}</p>}
              </div>
              <div>
                <label className="label-field">Phone Number</label>
                <input {...register('phone')} type="tel" className={`input-field ${errors.phone ? 'input-error' : ''}`} placeholder="+1 555 000 0000" />
                {errors.phone && <p className="text-xs text-red-500 mt-1">{errors.phone.message}</p>}
              </div>
              <div>
                <label className="label-field">Password</label>
                <input {...register('password')} type="password" className={`input-field ${errors.password ? 'input-error' : ''}`} placeholder="Min. 8 chars, 1 uppercase, 1 digit" />
                {errors.password && <p className="text-xs text-red-500 mt-1">{errors.password.message}</p>}
              </div>
            </div>
          </div>

          {/* License */}
          <div className="card">
            <h2 className="section-title mb-4 flex items-center gap-2">
              <span className="w-7 h-7 bg-indigo-100 text-indigo-600 rounded-full flex items-center justify-center text-sm font-bold">2</span>
              Driver's License
            </h2>
            <div>
              <label className="label-field">License Number</label>
              <input {...register('licenseNumber')} className={`input-field ${errors.licenseNumber ? 'input-error' : ''}`} placeholder="DL-1234567890" />
              {errors.licenseNumber && <p className="text-xs text-red-500 mt-1">{errors.licenseNumber.message}</p>}
              <p className="text-xs text-gray-400 mt-1">You'll need to upload a copy during verification</p>
            </div>
          </div>

          {/* Vehicle Info */}
          <div className="card">
            <h2 className="section-title mb-4 flex items-center gap-2">
              <span className="w-7 h-7 bg-indigo-100 text-indigo-600 rounded-full flex items-center justify-center text-sm font-bold">3</span>
              Vehicle Information
            </h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="label-field">Vehicle Type</label>
                <select {...register('vehicleType')} className={`input-field ${errors.vehicleType ? 'input-error' : ''}`}>
                  <option value="">Select type…</option>
                  {vehicleTypes.map(v => <option key={v.value} value={v.value}>{v.label}</option>)}
                </select>
                {errors.vehicleType && <p className="text-xs text-red-500 mt-1">{errors.vehicleType.message}</p>}
              </div>
              <div>
                <label className="label-field">Color</label>
                <input {...register('color')} className={`input-field ${errors.color ? 'input-error' : ''}`} placeholder="Silver" />
                {errors.color && <p className="text-xs text-red-500 mt-1">{errors.color.message}</p>}
              </div>
              <div>
                <label className="label-field">Make</label>
                <input {...register('make')} className={`input-field ${errors.make ? 'input-error' : ''}`} placeholder="Toyota" />
                {errors.make && <p className="text-xs text-red-500 mt-1">{errors.make.message}</p>}
              </div>
              <div>
                <label className="label-field">Model</label>
                <input {...register('model')} className={`input-field ${errors.model ? 'input-error' : ''}`} placeholder="Camry" />
                {errors.model && <p className="text-xs text-red-500 mt-1">{errors.model.message}</p>}
              </div>
              <div>
                <label className="label-field">Year</label>
                <input {...register('year')} type="number" className={`input-field ${errors.year ? 'input-error' : ''}`} placeholder="2021" min={2000} max={new Date().getFullYear() + 1} />
                {errors.year && <p className="text-xs text-red-500 mt-1">{errors.year.message}</p>}
              </div>
              <div>
                <label className="label-field">Registration Number</label>
                <input {...register('registrationNumber')} className={`input-field ${errors.registrationNumber ? 'input-error' : ''}`} placeholder="ABC-1234" />
                {errors.registrationNumber && <p className="text-xs text-red-500 mt-1">{errors.registrationNumber.message}</p>}
              </div>
              <div>
                <label className="label-field">Seating Capacity</label>
                <input {...register('seatingCapacity')} type="number" className={`input-field ${errors.seatingCapacity ? 'input-error' : ''}`} placeholder="4" min={2} max={12} />
                {errors.seatingCapacity && <p className="text-xs text-red-500 mt-1">{errors.seatingCapacity.message}</p>}
              </div>
            </div>
          </div>

          {serverError && (
            <div className="bg-red-50 border border-red-200 rounded-lg p-3 text-sm text-red-700">{serverError}</div>
          )}

          <button type="submit" disabled={isSubmitting} className="btn-primary w-full py-3 text-base">
            {isSubmitting ? (
              <span className="flex items-center justify-center gap-2">
                <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                Submitting Application…
              </span>
            ) : 'Submit Driver Application'}
          </button>

          <p className="text-center text-sm text-gray-500">
            Already have an account?{' '}
            <Link to="/login" className="text-indigo-600 hover:underline font-medium">Sign in</Link>
          </p>
        </form>
      </div>
    </div>
  );
}
