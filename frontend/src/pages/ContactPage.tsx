import React, { useState } from 'react';
import { Mail, Phone, MapPin, Send, CheckCircle2 } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function ContactPage() {
  const [submitted, setSubmitted] = useState(false);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [subject, setSubject] = useState('');
  const [message, setMessage] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitted(true);
  };

  return (
    <div className="max-w-4xl mx-auto px-4 py-12 fade-in">
      <div className="text-center mb-10">
        <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight sm:text-4xl">Contact Us</h1>
        <p className="mt-3 text-base text-gray-500 max-w-xl mx-auto">
          Have questions, partner inquiries, or feedback? We’d love to hear from you.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        {/* Contact Information Cards */}
        <div className="space-y-4">
          <div className="bg-white p-5 rounded-2xl border border-gray-200 shadow-sm">
            <div className="h-10 w-10 bg-indigo-50 text-indigo-600 rounded-xl flex items-center justify-center mb-3">
              <Mail className="h-5 w-5" />
            </div>
            <h3 className="font-semibold text-gray-900 text-sm">Customer Support</h3>
            <p className="text-xs text-gray-500 mt-1">For ride assistance & ticket queries</p>
            <p className="text-xs font-medium text-indigo-600 mt-2">support@cabflow.app</p>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-gray-200 shadow-sm">
            <div className="h-10 w-10 bg-emerald-50 text-emerald-600 rounded-xl flex items-center justify-center mb-3">
              <Phone className="h-5 w-5" />
            </div>
            <h3 className="font-semibold text-gray-900 text-sm">Emergency / Helpline</h3>
            <p className="text-xs text-gray-500 mt-1">24/7 Operations escalation</p>
            <p className="text-xs font-medium text-emerald-600 mt-2">+91 1800 200 4567</p>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-gray-200 shadow-sm">
            <div className="h-10 w-10 bg-purple-50 text-purple-600 rounded-xl flex items-center justify-center mb-3">
              <MapPin className="h-5 w-5" />
            </div>
            <h3 className="font-semibold text-gray-900 text-sm">Corporate Office</h3>
            <p className="text-xs text-gray-500 mt-1">Tech Hub, Cyber City, Bangalore, India</p>
            <Link to="/support" className="inline-block text-xs font-medium text-purple-600 mt-2 hover:underline">
              Visit Help Center &rarr;
            </Link>
          </div>
        </div>

        {/* Contact Form */}
        <div className="md:col-span-2 bg-white p-6 sm:p-8 rounded-2xl border border-gray-200 shadow-sm">
          {submitted ? (
            <div className="text-center py-12">
              <div className="h-14 w-14 bg-emerald-100 text-emerald-600 rounded-full flex items-center justify-center mx-auto mb-4">
                <CheckCircle2 className="h-8 w-8" />
              </div>
              <h3 className="text-lg font-bold text-gray-900">Message Received!</h3>
              <p className="text-xs text-gray-500 mt-2 max-w-sm mx-auto">
                Thank you for contacting CabFlow. Our operations team has received your inquiry and will respond within 24 hours.
              </p>
              <button
                onClick={() => setSubmitted(false)}
                className="mt-6 text-xs font-semibold text-indigo-600 hover:text-indigo-800"
              >
                Send another message
              </button>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-4">
              <h3 className="font-bold text-gray-900 text-base mb-2">Send us a message</h3>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Full Name</label>
                  <input
                    type="text"
                    required
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="Your Name"
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Email Address</label>
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="you@domain.com"
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Subject</label>
                <input
                  type="text"
                  required
                  value={subject}
                  onChange={(e) => setSubject(e.target.value)}
                  placeholder="Inquiry topic"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Message</label>
                <textarea
                  required
                  rows={4}
                  value={message}
                  onChange={(e) => setMessage(e.target.value)}
                  placeholder="How can we assist you?"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500"
                />
              </div>

              <button
                type="submit"
                className="w-full bg-slate-900 hover:bg-slate-800 text-white font-medium py-2.5 rounded-xl text-sm transition flex items-center justify-center gap-2"
              >
                <Send className="h-4 w-4" />
                <span>Submit Inquiry</span>
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}
