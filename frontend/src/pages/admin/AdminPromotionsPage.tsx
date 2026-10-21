import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { adminService } from '../../services/adminService';
import { AdminPromoCode, PromoRedemption } from '../../types/admin';

export default function AdminPromotionsPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [selectedPromoForRedemptions, setSelectedPromoForRedemptions] = useState<AdminPromoCode | null>(null);
  const [redemptionsPage, setRedemptionsPage] = useState(0);

  // Form states
  const [code, setCode] = useState('');
  const [discountType, setDiscountType] = useState<'PERCENTAGE' | 'FIXED_AMOUNT'>('PERCENTAGE');
  const [discountValue, setDiscountValue] = useState<number>(10);
  const [minimumFare, setMinimumFare] = useState<number>(0);
  const [maximumDiscount, setMaximumDiscount] = useState<number | undefined>(undefined);
  const [validUntil, setValidUntil] = useState('');
  const [usageLimit, setUsageLimit] = useState<number>(100);
  const [formError, setFormError] = useState<string | null>(null);

  const { data, isLoading } = useQuery({
    queryKey: ['adminPromotions', page],
    queryFn: () => adminService.getPromotions(page, 10),
  });

  const { data: redemptionsData, isLoading: isLoadingRedemptions } = useQuery({
    queryKey: ['adminPromoRedemptions', selectedPromoForRedemptions?.id, redemptionsPage],
    queryFn: () => adminService.getPromoRedemptions(selectedPromoForRedemptions!.id, redemptionsPage, 10),
    enabled: !!selectedPromoForRedemptions,
  });

  const createMutation = useMutation({
    mutationFn: (newPromo: any) => adminService.createPromotion(newPromo),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminPromotions'] });
      setShowCreateModal(false);
      resetForm();
    },
    onError: (err: any) => {
      setFormError(err.response?.data?.message || 'Failed to create promotion');
    },
  });

  const toggleMutation = useMutation({
    mutationFn: (promoId: number) => adminService.togglePromotion(promoId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminPromotions'] });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (promoId: number) => adminService.deletePromotion(promoId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminPromotions'] });
    },
  });

  const resetForm = () => {
    setCode('');
    setDiscountType('PERCENTAGE');
    setDiscountValue(10);
    setMinimumFare(0);
    setMaximumDiscount(undefined);
    setValidUntil('');
    setUsageLimit(100);
    setFormError(null);
  };

  const handleCreateSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);

    if (!code.trim()) {
      setFormError('Promo code is required');
      return;
    }
    if (!validUntil) {
      setFormError('Expiration date is required');
      return;
    }

    createMutation.mutate({
      code: code.trim().toUpperCase(),
      discountType,
      discountValue: Number(discountValue),
      minimumFare: Number(minimumFare),
      maximumDiscount: maximumDiscount ? Number(maximumDiscount) : undefined,
      validUntil: new Date(validUntil).toISOString(),
      usageLimit: Number(usageLimit),
    });
  };

  const promotions = (data?.content || []) as AdminPromoCode[];
  const totalPages = data?.totalPages || 0;
  const redemptions = (redemptionsData?.content || []) as PromoRedemption[];
  const redemptionsTotalPages = redemptionsData?.totalPages || 0;

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-6 border-b border-gray-200 mb-6 gap-4">
        <div>
          <div className="flex items-center gap-2 text-sm text-gray-500 mb-1">
            <Link to="/admin/dashboard" className="hover:text-indigo-600">Admin</Link>
            <span>/</span>
            <span className="text-gray-900 font-medium">Promotions</span>
          </div>
          <h1 className="text-2xl font-bold text-gray-900">Promotion & Coupon Management</h1>
          <p className="text-sm text-gray-500 mt-0.5">Create discounts, set expiration dates, limit usage, and track redemptions</p>
        </div>

        <button
          onClick={() => { resetForm(); setShowCreateModal(true); }}
          className="inline-flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 shadow-sm transition"
        >
          <span>+ Create New Promo</span>
        </button>
      </div>

      {/* Promotions Table */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden mb-8">
        {isLoading ? (
          <div className="p-8 text-center text-gray-500">
            <div className="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin mx-auto mb-2" />
            Loading promotions...
          </div>
        ) : promotions.length === 0 ? (
          <div className="p-12 text-center text-gray-500">
            <p className="text-lg font-medium text-gray-700">No promo codes created yet</p>
            <p className="text-sm mt-1">Click "Create New Promo" above to launch your first marketing campaign.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-gray-50 border-b border-gray-200 text-gray-600 uppercase text-xs">
                <tr>
                  <th className="px-6 py-3 font-semibold">Promo Code</th>
                  <th className="px-6 py-3 font-semibold">Discount</th>
                  <th className="px-6 py-3 font-semibold">Min Fare / Cap</th>
                  <th className="px-6 py-3 font-semibold">Usage</th>
                  <th className="px-6 py-3 font-semibold">Valid Until</th>
                  <th className="px-6 py-3 font-semibold">Status</th>
                  <th className="px-6 py-3 font-semibold text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {promotions.map((p) => (
                  <tr key={p.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-6 py-4">
                      <span className="font-mono font-bold text-indigo-700 bg-indigo-50 px-2.5 py-1 rounded-md border border-indigo-200">
                        {p.code}
                      </span>
                    </td>
                    <td className="px-6 py-4 font-medium text-gray-900">
                      {p.discountType === 'PERCENTAGE' ? `${p.discountValue}% OFF` : `₹${p.discountValue} OFF`}
                    </td>
                    <td className="px-6 py-4 text-xs text-gray-500">
                      <div>Min: ₹{p.minimumFare || 0}</div>
                      {p.maximumDiscount && <div>Max Cap: ₹{p.maximumDiscount}</div>}
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-xs text-gray-900 font-medium">
                        {p.timesUsed || 0} / {p.usageLimit || '∞'}
                      </div>
                      <div className="w-24 bg-gray-200 rounded-full h-1.5 mt-1 overflow-hidden">
                        <div
                          className="bg-indigo-600 h-1.5 rounded-full"
                          style={{
                            width: `${Math.min(100, ((p.timesUsed || 0) / (p.usageLimit || 1)) * 100)}%`,
                          }}
                        />
                      </div>
                    </td>
                    <td className="px-6 py-4 text-xs text-gray-500 whitespace-nowrap">
                      {new Date(p.validUntil).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4">
                      <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold ${
                        p.active ? 'bg-emerald-100 text-emerald-800' : 'bg-gray-100 text-gray-600'
                      }`}>
                        {p.active ? 'ACTIVE' : 'INACTIVE'}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right space-x-2 whitespace-nowrap">
                      <button
                        onClick={() => setSelectedPromoForRedemptions(p)}
                        className="text-xs text-indigo-600 hover:text-indigo-800 font-medium bg-indigo-50 px-2.5 py-1 rounded"
                      >
                        Redemptions
                      </button>
                      <button
                        onClick={() => toggleMutation.mutate(p.id)}
                        className="text-xs text-amber-600 hover:text-amber-800 font-medium bg-amber-50 px-2.5 py-1 rounded"
                      >
                        {p.active ? 'Deactivate' : 'Activate'}
                      </button>
                      <button
                        onClick={() => {
                          if (confirm(`Are you sure you want to delete promo ${p.code}?`)) {
                            deleteMutation.mutate(p.id);
                          }
                        }}
                        className="text-xs text-rose-600 hover:text-rose-800 font-medium bg-rose-50 px-2.5 py-1 rounded"
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="px-6 py-4 bg-gray-50 border-t border-gray-200 flex items-center justify-between">
            <span className="text-xs text-gray-500">
              Page {page + 1} of {totalPages}
            </span>
            <div className="flex gap-2">
              <button
                disabled={page === 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                className="px-3 py-1 text-xs font-medium border border-gray-300 rounded bg-white disabled:opacity-50 hover:bg-gray-100"
              >
                Previous
              </button>
              <button
                disabled={page >= totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
                className="px-3 py-1 text-xs font-medium border border-gray-300 rounded bg-white disabled:opacity-50 hover:bg-gray-100"
              >
                Next
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Redemptions Modal / Drawer */}
      {selectedPromoForRedemptions && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl max-w-3xl w-full p-6 shadow-xl border border-gray-200 max-h-[85vh] flex flex-col">
            <div className="flex items-center justify-between pb-4 border-b border-gray-200">
              <div>
                <h3 className="text-lg font-bold text-gray-900">
                  Redemption History: <span className="text-indigo-600 font-mono">{selectedPromoForRedemptions.code}</span>
                </h3>
                <p className="text-xs text-gray-500">Track all riders who redeemed this promo</p>
              </div>
              <button
                onClick={() => setSelectedPromoForRedemptions(null)}
                className="text-gray-400 hover:text-gray-600 text-lg font-bold"
              >
                ✕
              </button>
            </div>

            <div className="flex-1 overflow-y-auto py-4">
              {isLoadingRedemptions ? (
                <div className="p-8 text-center text-gray-500">Loading redemptions...</div>
              ) : redemptions.length === 0 ? (
                <div className="p-8 text-center text-gray-500">No redemptions recorded for this promo yet.</div>
              ) : (
                <table className="w-full text-left text-xs">
                  <thead className="bg-gray-50 uppercase text-gray-600">
                    <tr>
                      <th className="px-4 py-2">Rider</th>
                      <th className="px-4 py-2">Ride ID</th>
                      <th className="px-4 py-2">Discount</th>
                      <th className="px-4 py-2">Date</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {redemptions.map((r) => (
                      <tr key={r.id}>
                        <td className="px-4 py-3">
                          <div className="font-semibold text-gray-900">{r.userName}</div>
                          <div className="text-gray-500 text-[11px]">{r.userEmail}</div>
                        </td>
                        <td className="px-4 py-3 font-mono text-indigo-600">#{r.rideId}</td>
                        <td className="px-4 py-3 font-bold text-emerald-600">₹{r.discountAmount}</td>
                        <td className="px-4 py-3 text-gray-500">{new Date(r.redeemedAt).toLocaleString()}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>

            {redemptionsTotalPages > 1 && (
              <div className="pt-3 border-t border-gray-200 flex justify-between items-center text-xs">
                <span>Page {redemptionsPage + 1} of {redemptionsTotalPages}</span>
                <div className="flex gap-2">
                  <button
                    disabled={redemptionsPage === 0}
                    onClick={() => setRedemptionsPage((p) => Math.max(0, p - 1))}
                    className="px-2 py-1 border rounded disabled:opacity-50"
                  >
                    Prev
                  </button>
                  <button
                    disabled={redemptionsPage >= redemptionsTotalPages - 1}
                    onClick={() => setRedemptionsPage((p) => p + 1)}
                    className="px-2 py-1 border rounded disabled:opacity-50"
                  >
                    Next
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Create Promo Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl max-w-md w-full p-6 shadow-xl border border-gray-200">
            <h3 className="text-lg font-bold text-gray-900 mb-1">Create Promotion</h3>
            <p className="text-xs text-gray-500 mb-4">Launch a new promotional coupon for riders</p>

            {formError && (
              <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg">
                {formError}
              </div>
            )}

            <form onSubmit={handleCreateSubmit} className="space-y-4 text-sm">
              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                  Promo Code *
                </label>
                <input
                  type="text"
                  required
                  placeholder="e.g. MONSOON50"
                  value={code}
                  onChange={(e) => setCode(e.target.value.toUpperCase())}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 uppercase font-mono font-bold focus:ring-2 focus:ring-indigo-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                    Discount Type
                  </label>
                  <select
                    value={discountType}
                    onChange={(e) => setDiscountType(e.target.value as any)}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 bg-white"
                  >
                    <option value="PERCENTAGE">Percentage (%)</option>
                    <option value="FIXED_AMOUNT">Flat Amount (₹)</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                    Discount Value *
                  </label>
                  <input
                    type="number"
                    min="1"
                    required
                    value={discountValue}
                    onChange={(e) => setDiscountValue(Number(e.target.value))}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                    Min Fare (₹)
                  </label>
                  <input
                    type="number"
                    min="0"
                    value={minimumFare}
                    onChange={(e) => setMinimumFare(Number(e.target.value))}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                    Max Cap (₹)
                  </label>
                  <input
                    type="number"
                    min="0"
                    placeholder="Optional"
                    value={maximumDiscount ?? ''}
                    onChange={(e) => setMaximumDiscount(e.target.value ? Number(e.target.value) : undefined)}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                    Usage Limit
                  </label>
                  <input
                    type="number"
                    min="1"
                    value={usageLimit}
                    onChange={(e) => setUsageLimit(Number(e.target.value))}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                    Valid Until *
                  </label>
                  <input
                    type="date"
                    required
                    value={validUntil}
                    onChange={(e) => setValidUntil(e.target.value)}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  />
                </div>
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-gray-200">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 text-sm text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-lg"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={createMutation.isPending}
                  className="px-4 py-2 text-sm text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg font-medium disabled:opacity-50"
                >
                  {createMutation.isPending ? 'Creating...' : 'Create Promo'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
