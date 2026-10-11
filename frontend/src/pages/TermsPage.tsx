export default function TermsPage() {
  return (
    <div className="max-w-3xl mx-auto px-4 py-12 fade-in">
      <h1 className="page-title mb-2">Terms of Service & Privacy Policy</h1>
      <p className="text-gray-400 text-sm mb-8">Last updated: January 2025</p>

      <div className="prose prose-gray max-w-none space-y-6">
        <section className="card">
          <h2 className="section-title mb-3">1. Acceptance of Terms</h2>
          <p className="text-gray-600 text-sm leading-relaxed">
            By creating an account or using CabFlow services, you agree to be bound by these Terms of Service.
            If you do not agree to these terms, you may not use our platform.
          </p>
        </section>

        <section className="card">
          <h2 className="section-title mb-3">2. Use of Service</h2>
          <p className="text-gray-600 text-sm leading-relaxed">
            CabFlow provides a technology platform connecting riders with independent drivers. We are not a
            transportation company. Drivers are independent contractors, not employees of CabFlow. You must be
            at least 18 years of age to use our service.
          </p>
        </section>

        <section className="card">
          <h2 className="section-title mb-3">3. User Accounts</h2>
          <p className="text-gray-600 text-sm leading-relaxed">
            You are responsible for maintaining the confidentiality of your account credentials. You agree to
            immediately notify CabFlow of any unauthorized use of your account. CabFlow reserves the right to
            suspend accounts that violate these terms.
          </p>
        </section>

        <section className="card">
          <h2 className="section-title mb-3">4. Privacy Policy</h2>
          <p className="text-gray-600 text-sm leading-relaxed">
            We collect information you provide directly (name, email, phone, payment info) and information about
            your use of the service (ride history, location data during rides). We use this data to provide and
            improve our services, process payments, and communicate with you. We do not sell your personal data
            to third parties. Location data is only collected during active rides.
          </p>
        </section>

        <section className="card">
          <h2 className="section-title mb-3">5. Payments & Refunds</h2>
          <p className="text-gray-600 text-sm leading-relaxed">
            All payments are processed securely via Stripe. Fares are calculated based on distance, time, and
            vehicle type. Cancellation fees may apply. Refund requests are evaluated on a case-by-case basis
            through our support team.
          </p>
        </section>

        <section className="card">
          <h2 className="section-title mb-3">6. Limitation of Liability</h2>
          <p className="text-gray-600 text-sm leading-relaxed">
            CabFlow shall not be liable for any indirect, incidental, or consequential damages arising from your
            use of the service. Our liability is limited to the amount paid for the specific ride in question.
          </p>
        </section>

        <section className="card">
          <h2 className="section-title mb-3">7. Contact</h2>
          <p className="text-gray-600 text-sm leading-relaxed">
            For questions about these terms, please contact us at{' '}
            <a href="mailto:legal@cabflow.app" className="text-indigo-600 hover:underline">legal@cabflow.app</a>.
          </p>
        </section>
      </div>
    </div>
  );
}
