import { Link } from 'react-router-dom';
import { ShieldCheck, Award, Compass } from 'lucide-react';

export default function AboutPage() {
  return (
    <div className="max-w-4xl mx-auto px-4 py-12 fade-in">
      <div className="text-center mb-10">
        <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight sm:text-4xl">About CabFlow</h1>
        <p className="mt-3 text-base text-gray-500 max-w-2xl mx-auto">
          Building India's most dependable, transparent, and seamless urban mobility platform.
        </p>
      </div>

      <div className="space-y-8">
        <div className="bg-white rounded-2xl p-6 sm:p-8 border border-gray-100 shadow-sm">
          <h2 className="text-xl font-bold text-gray-900 mb-3">Our Mission</h2>
          <p className="text-gray-600 leading-relaxed text-sm">
            At CabFlow, we believe transportation should be effortless, secure, and fair for both riders and drivers. 
            By leveraging state-of-the-art dispatch algorithms, real-time GPS telemetry, and instant cashless settlements via Razorpay, 
            we empower commuters to move freely while giving drivers dignifying earnings and independence.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-emerald-50 rounded-2xl p-6 border border-emerald-100">
            <div className="h-10 w-10 bg-emerald-500 text-white rounded-xl flex items-center justify-center mb-4">
              <ShieldCheck className="h-5 w-5" />
            </div>
            <h3 className="font-bold text-gray-900 mb-2">Safety First</h3>
            <p className="text-xs text-gray-600 leading-relaxed">
              Every driver undergoes document and background verification before getting on the road. Features like real-time tracking and Emergency SOS ensure peace of mind.
            </p>
          </div>

          <div className="bg-indigo-50 rounded-2xl p-6 border border-indigo-100">
            <div className="h-10 w-10 bg-indigo-600 text-white rounded-xl flex items-center justify-center mb-4">
              <Award className="h-5 w-5" />
            </div>
            <h3 className="font-bold text-gray-900 mb-2">Fair & Upfront</h3>
            <p className="text-xs text-gray-600 leading-relaxed">
              No hidden surge surprises. Clear fare breakdowns in INR, promotional discounts, and automated downloadable PDF tax receipts for every trip.
            </p>
          </div>

          <div className="bg-purple-50 rounded-2xl p-6 border border-purple-100">
            <div className="h-10 w-10 bg-purple-600 text-white rounded-xl flex items-center justify-center mb-4">
              <Compass className="h-5 w-5" />
            </div>
            <h3 className="font-bold text-gray-900 mb-2">Instant Dispatch</h3>
            <p className="text-xs text-gray-600 leading-relaxed">
              Automated geospatial matching pairs riders with nearest verified drivers instantly or schedules rides up to 7 days ahead.
            </p>
          </div>
        </div>

        <div className="bg-slate-900 text-white rounded-2xl p-6 sm:p-8">
          <div className="flex flex-col sm:flex-row items-center justify-between gap-6">
            <div>
              <h3 className="text-lg font-bold">Ready to ride or drive with us?</h3>
              <p className="text-xs text-slate-300 mt-1">Join thousands of riders and driver-partners moving forward together.</p>
            </div>
            <div className="flex gap-3">
              <Link to="/register" className="bg-emerald-500 hover:bg-emerald-600 text-white text-xs font-semibold px-4 py-2.5 rounded-xl transition">
                Book a Ride
              </Link>
              <Link to="/driver/register" className="bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 text-xs font-semibold px-4 py-2.5 rounded-xl transition">
                Become a Driver
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
