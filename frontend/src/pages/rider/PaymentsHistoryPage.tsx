import { useEffect, useState } from 'react';
import { paymentService } from '../../services/paymentService';
import { PaymentTransaction } from '../../types/payment';
import { formatCurrency } from '../../utils/formatters';
import { CreditCard, Download, ExternalLink, CheckCircle2, Clock, XCircle, ArrowRight } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function PaymentsHistoryPage() {
  const [payments, setPayments] = useState<PaymentTransaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [downloadingId, setDownloadingId] = useState<number | null>(null);

  const fetchPayments = async (p = 0) => {
    setLoading(true);
    try {
      const res = await paymentService.getMyPayments(p, 10);
      setPayments(res.content || []);
      setTotalPages(res.totalPages || 1);
      setPage(p);
    } catch (err) {
      console.error('Failed to load payments history:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPayments(0);
  }, []);

  const handleDownloadReceipt = async (rideId: number) => {
    setDownloadingId(rideId);
    try {
      const blob = await paymentService.downloadReceiptPdf(rideId);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `receipt_ride_${rideId}.pdf`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (err) {
      alert('Could not download receipt. Please make sure the ride is completed.');
    } finally {
      setDownloadingId(null);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return (
          <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
            <CheckCircle2 className="w-3.5 h-3.5 mr-1" /> Paid
          </span>
        );
      case 'REFUNDED':
        return (
          <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-purple-50 text-purple-700 border border-purple-200">
            <Clock className="w-3.5 h-3.5 mr-1" /> Refunded
          </span>
        );
      case 'FAILED':
        return (
          <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-rose-50 text-rose-700 border border-rose-200">
            <XCircle className="w-3.5 h-3.5 mr-1" /> Failed
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-50 text-amber-700 border border-amber-200">
            <Clock className="w-3.5 h-3.5 mr-1" /> Pending
          </span>
        );
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-5xl mx-auto space-y-6">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">Payments & Receipts</h1>
            <p className="mt-1 text-sm text-slate-500">
              Review your completed ride payments, transaction identifiers, and official receipts.
            </p>
          </div>
          <Link
            to="/rider/history"
            className="inline-flex items-center text-sm font-semibold text-indigo-600 hover:text-indigo-800"
          >
            View Ride History <ArrowRight className="w-4 h-4 ml-1" />
          </Link>
        </div>

        {/* Content Table / Cards */}
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
          {loading ? (
            <div className="py-20 flex flex-col items-center justify-center space-y-3">
              <div className="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin" />
              <p className="text-sm font-medium text-slate-500">Loading transactions...</p>
            </div>
          ) : payments.length === 0 ? (
            <div className="py-16 text-center px-4">
              <CreditCard className="w-12 h-12 text-slate-300 mx-auto mb-3" />
              <h3 className="text-base font-bold text-slate-800">No payment records found</h3>
              <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
                Once you complete your trips and checkout via Razorpay, your payment receipts will appear here.
              </p>
              <div className="mt-6">
                <Link
                  to="/rider/book"
                  className="inline-flex items-center px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-semibold shadow-sm transition"
                >
                  Book a Ride Now
                </Link>
              </div>
            </div>
          ) : (
            <div className="divide-y divide-slate-100">
              <div className="hidden md:grid grid-cols-12 gap-4 px-6 py-3.5 bg-slate-50 text-xs font-bold text-slate-500 uppercase tracking-wider">
                <div className="col-span-2">Date</div>
                <div className="col-span-4">Trip Details</div>
                <div className="col-span-2">Amount</div>
                <div className="col-span-2">Status</div>
                <div className="col-span-2 text-right">Receipt</div>
              </div>

              {payments.map((p) => (
                <div
                  key={p.id}
                  className="p-6 md:px-6 md:py-4 md:grid md:grid-cols-12 md:gap-4 md:items-center hover:bg-slate-50/60 transition"
                >
                  {/* Date & Provider */}
                  <div className="col-span-2 mb-2 md:mb-0">
                    <p className="text-sm font-semibold text-slate-900">
                      {new Date(p.createdAt).toLocaleDateString(undefined, {
                        month: 'short',
                        day: 'numeric',
                        year: 'numeric',
                      })}
                    </p>
                    <p className="text-xs text-slate-400 mt-0.5 font-mono">
                      {p.providerPaymentId || p.paymentProvider}
                    </p>
                  </div>

                  {/* Trip Info */}
                  <div className="col-span-4 mb-3 md:mb-0 space-y-1">
                    <div className="flex items-center text-xs font-semibold text-slate-700">
                      <span className="w-2 h-2 rounded-full bg-emerald-500 mr-2 flex-shrink-0" />
                      <span className="truncate">{p.pickupAddress || 'Pickup location'}</span>
                    </div>
                    <div className="flex items-center text-xs font-semibold text-slate-700">
                      <span className="w-2 h-2 rounded-full bg-rose-500 mr-2 flex-shrink-0" />
                      <span className="truncate">{p.dropoffAddress || 'Destination location'}</span>
                    </div>
                    {p.rideId && (
                      <Link
                        to={`/rider/ride/${p.rideId}`}
                        className="inline-flex items-center text-[11px] font-medium text-indigo-600 hover:underline pt-0.5"
                      >
                        Ride #{p.rideId} details <ExternalLink className="w-3 h-3 ml-0.5" />
                      </Link>
                    )}
                  </div>

                  {/* Amount */}
                  <div className="col-span-2 mb-2 md:mb-0">
                    <p className="text-base font-extrabold text-slate-900">
                      {formatCurrency(p.amount, p.currency)}
                    </p>
                    {p.refundedAmount && p.refundedAmount > 0 && (
                      <p className="text-[11px] font-medium text-purple-600">
                        Refunded: {formatCurrency(p.refundedAmount, p.currency)}
                      </p>
                    )}
                  </div>

                  {/* Status */}
                  <div className="col-span-2 mb-3 md:mb-0">
                    {getStatusBadge(p.paymentStatus)}
                  </div>

                  {/* Action / Receipt */}
                  <div className="col-span-2 md:text-right">
                    {p.rideId && p.paymentStatus === 'COMPLETED' ? (
                      <button
                        onClick={() => handleDownloadReceipt(p.rideId!)}
                        disabled={downloadingId === p.rideId}
                        className="inline-flex items-center px-3 py-1.5 rounded-xl text-xs font-semibold bg-slate-100 hover:bg-indigo-50 hover:text-indigo-600 text-slate-700 border border-slate-200 transition disabled:opacity-50"
                      >
                        <Download className="w-3.5 h-3.5 mr-1.5" />
                        {downloadingId === p.rideId ? 'Downloading...' : 'PDF Receipt'}
                      </button>
                    ) : (
                      <span className="text-xs text-slate-400 italic">Receipt unavailable</span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="p-4 bg-slate-50 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
              <button
                disabled={page === 0}
                onClick={() => fetchPayments(page - 1)}
                className="px-3 py-1.5 rounded-lg border border-slate-200 bg-white font-medium hover:bg-slate-50 disabled:opacity-40"
              >
                Previous
              </button>
              <span>
                Page {page + 1} of {totalPages}
              </span>
              <button
                disabled={page >= totalPages - 1}
                onClick={() => fetchPayments(page + 1)}
                className="px-3 py-1.5 rounded-lg border border-slate-200 bg-white font-medium hover:bg-slate-50 disabled:opacity-40"
              >
                Next
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
