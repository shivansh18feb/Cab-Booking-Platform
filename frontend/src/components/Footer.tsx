import React from 'react';
import { Link } from 'react-router-dom';
import { Car, ShieldCheck, HeartHandshake, PhoneCall } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-slate-950 text-slate-400 border-t border-slate-900 py-12">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 grid grid-cols-1 md:grid-cols-4 gap-8">
        {/* Brand Summary */}
        <div className="space-y-4">
          <div className="flex items-center space-x-2">
            <div className="h-8 w-8 rounded-lg bg-emerald-500 flex items-center justify-center text-white">
              <Car className="h-5 w-5" />
            </div>
            <span className="text-xl font-bold text-white">
              Cab<span className="text-emerald-400">Flow</span>
            </span>
          </div>
          <p className="text-sm text-slate-400 leading-relaxed">
            Next-generation on-demand urban mobility. Reliable dispatch, transparent fares, live GPS tracking, and secure digital payments.
          </p>
        </div>

        {/* Quick Links */}
        <div>
          <h4 className="text-sm font-semibold text-white uppercase tracking-wider mb-4">Riders</h4>
          <ul className="space-y-2 text-sm">
            <li><Link to="/rider/dashboard" className="hover:text-emerald-400 transition">Book a Ride</Link></li>
            <li><Link to="/promotions" className="hover:text-emerald-400 transition">Promo Codes & Discounts</Link></li>
            <li><Link to="/rider/history" className="hover:text-emerald-400 transition">Ride History & Receipts</Link></li>
            <li><Link to="/support" className="hover:text-emerald-400 transition">Lost & Found / Help</Link></li>
          </ul>
        </div>

        {/* Drivers */}
        <div>
          <h4 className="text-sm font-semibold text-white uppercase tracking-wider mb-4">Drivers</h4>
          <ul className="space-y-2 text-sm">
            <li><Link to="/driver/register" className="hover:text-emerald-400 transition">Drive with CabFlow</Link></li>
            <li><Link to="/driver/dashboard" className="hover:text-emerald-400 transition">Driver Portal</Link></li>
            <li><Link to="/driver/earnings" className="hover:text-emerald-400 transition">Weekly Payouts & Commission</Link></li>
            <li><Link to="/terms-privacy" className="hover:text-emerald-400 transition">Vehicle Standards</Link></li>
          </ul>
        </div>

        {/* Safety & Compliance */}
        <div>
          <h4 className="text-sm font-semibold text-white uppercase tracking-wider mb-4">Safety & Legal</h4>
          <ul className="space-y-2 text-sm">
            <li className="flex items-center space-x-2">
              <ShieldCheck className="h-4 w-4 text-emerald-400" />
              <span>Background-Checked Drivers</span>
            </li>
            <li className="flex items-center space-x-2">
              <HeartHandshake className="h-4 w-4 text-teal-400" />
              <span>24/7 Incident Escalation</span>
            </li>
            <li className="flex items-center space-x-2">
              <PhoneCall className="h-4 w-4 text-rose-400" />
              <span>Emergency SOS Assist</span>
            </li>
            <li className="pt-2 flex flex-wrap gap-x-3 gap-y-1 text-xs text-slate-500">
              <Link to="/about" className="hover:text-slate-300 transition">About</Link>
              <span>•</span>
              <Link to="/contact" className="hover:text-slate-300 transition">Contact</Link>
              <span>•</span>
              <Link to="/privacy" className="hover:text-slate-300 transition">Privacy</Link>
              <span>•</span>
              <Link to="/terms" className="hover:text-slate-300 transition">Terms</Link>
            </li>
          </ul>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-10 pt-6 border-t border-slate-900 text-center text-xs text-slate-500">
        &copy; {new Date().getFullYear()} CabFlow Inc. All rights reserved. Built with Java 21 LTS, Spring Boot 3, and React.
      </div>
    </footer>
  );
};

export default Footer;
