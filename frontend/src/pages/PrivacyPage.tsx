import { Link } from 'react-router-dom';
import { Shield, Lock, Eye, FileText } from 'lucide-react';

export default function PrivacyPage() {
  return (
    <div className="max-w-4xl mx-auto px-4 py-12 fade-in">
      <div className="text-center mb-10">
        <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight sm:text-4xl">Privacy Policy</h1>
        <p className="mt-3 text-base text-gray-500 max-w-xl mx-auto">
          How CabFlow protects, handles, and secures your personal and location data.
        </p>
      </div>

      <div className="prose prose-gray max-w-none space-y-6">
        <section className="bg-white p-6 rounded-2xl border border-gray-200 shadow-sm">
          <div className="flex items-center gap-3 mb-3">
            <div className="p-2 bg-indigo-50 text-indigo-600 rounded-lg">
              <Lock className="h-5 w-5" />
            </div>
            <h2 className="text-lg font-bold text-gray-900 m-0">1. Data Collection & Usage</h2>
          </div>
          <p className="text-gray-600 text-sm leading-relaxed">
            We collect the personal information you provide when registering: full name, verified email, telephone number, and payment credentials. 
            Payment credentials and card numbers are processed directly through Razorpay's PCI-DSS Level 1 compliant gateway and never stored unencrypted on our servers.
          </p>
        </section>

        <section className="bg-white p-6 rounded-2xl border border-gray-200 shadow-sm">
          <div className="flex items-center gap-3 mb-3">
            <div className="p-2 bg-emerald-50 text-emerald-600 rounded-lg">
              <Eye className="h-5 w-5" />
            </div>
            <h2 className="text-lg font-bold text-gray-900 m-0">2. Real-Time Telemetry & GPS</h2>
          </div>
          <p className="text-gray-600 text-sm leading-relaxed">
            CabFlow captures driver location continuously when a driver is marked "ONLINE" to enable dispatching. 
            Rider GPS coordinates are only gathered when requesting a ride or actively tracking an assigned driver. We never monetize, rent, or sell location datasets to advertisers.
          </p>
        </section>

        <section className="bg-white p-6 rounded-2xl border border-gray-200 shadow-sm">
          <div className="flex items-center gap-3 mb-3">
            <div className="p-2 bg-amber-50 text-amber-600 rounded-lg">
              <Shield className="h-5 w-5" />
            </div>
            <h2 className="text-lg font-bold text-gray-900 m-0">3. Driver Document Verification</h2>
          </div>
          <p className="text-gray-600 text-sm leading-relaxed">
            Driver-partners submit driving licenses and vehicle registration certificates for regulatory compliance and rider security. 
            All documents are stored securely with restricted access reserved solely for authorized compliance auditors.
          </p>
        </section>

        <section className="bg-white p-6 rounded-2xl border border-gray-200 shadow-sm">
          <div className="flex items-center gap-3 mb-3">
            <div className="p-2 bg-purple-50 text-purple-600 rounded-lg">
              <FileText className="h-5 w-5" />
            </div>
            <h2 className="text-lg font-bold text-gray-900 m-0">4. Your Privacy Rights</h2>
          </div>
          <p className="text-gray-600 text-sm leading-relaxed">
            You may request an export of your ride history, update your personal profile directly in the Rider Settings tab, or request account closure at any time through our{' '}
            <Link to="/support" className="text-indigo-600 hover:underline">Customer Support desk</Link>.
          </p>
        </section>
      </div>
    </div>
  );
}
