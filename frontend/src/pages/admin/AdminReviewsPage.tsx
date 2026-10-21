import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { adminService } from '../../services/adminService';
import { AdminReview } from '../../types/admin';

export default function AdminReviewsPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [hiddenFilter, setHiddenFilter] = useState<string>('');
  const [starsFilter, setStarsFilter] = useState<string>('');

  const isHiddenParam = hiddenFilter === '' ? undefined : hiddenFilter === 'true';
  const starsParam = starsFilter === '' ? undefined : Number(starsFilter);

  const { data, isLoading } = useQuery({
    queryKey: ['adminReviews', page, hiddenFilter, starsFilter],
    queryFn: () => adminService.getReviews(page, 10, isHiddenParam, starsParam),
  });

  const toggleVisibilityMutation = useMutation({
    mutationFn: ({ id, hide }: { id: number; hide: boolean }) =>
      adminService.toggleReviewVisibility(id, hide),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminReviews'] });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => adminService.deleteReview(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminReviews'] });
    },
  });

  const reviews = (data?.content || []) as AdminReview[];
  const totalPages = data?.totalPages || 0;

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-6 border-b border-gray-200 mb-6 gap-4">
        <div>
          <div className="flex items-center gap-2 text-sm text-gray-500 mb-1">
            <Link to="/admin/dashboard" className="hover:text-indigo-600">Admin</Link>
            <span>/</span>
            <span className="text-gray-900 font-medium">Review Moderation</span>
          </div>
          <h1 className="text-2xl font-bold text-gray-900">Review Moderation</h1>
          <p className="text-sm text-gray-500 mt-0.5">Audit rider ratings and comments, hide abusive reviews, or permanently delete spam</p>
        </div>

        {/* Filters */}
        <div className="flex flex-wrap items-center gap-3">
          <select
            value={starsFilter}
            onChange={(e) => { setStarsFilter(e.target.value); setPage(0); }}
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm bg-white"
          >
            <option value="">All Star Ratings</option>
            <option value="5">5 Stars</option>
            <option value="4">4 Stars</option>
            <option value="3">3 Stars</option>
            <option value="2">2 Stars</option>
            <option value="1">1 Star</option>
          </select>

          <select
            value={hiddenFilter}
            onChange={(e) => { setHiddenFilter(e.target.value); setPage(0); }}
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm bg-white"
          >
            <option value="">All Visibility</option>
            <option value="false">Visible Only</option>
            <option value="true">Hidden / Moderated Only</option>
          </select>
        </div>
      </div>

      {/* Reviews Table */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        {isLoading ? (
          <div className="p-8 text-center text-gray-500">
            <div className="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin mx-auto mb-2" />
            Loading reviews...
          </div>
        ) : reviews.length === 0 ? (
          <div className="p-12 text-center text-gray-500">
            <p className="text-lg font-medium text-gray-700">No reviews found</p>
            <p className="text-sm mt-1">There are no reviews matching your selected criteria.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-gray-50 border-b border-gray-200 text-gray-600 uppercase text-xs">
                <tr>
                  <th className="px-6 py-3 font-semibold">Ride</th>
                  <th className="px-6 py-3 font-semibold">Rider</th>
                  <th className="px-6 py-3 font-semibold">Driver</th>
                  <th className="px-6 py-3 font-semibold">Stars</th>
                  <th className="px-6 py-3 font-semibold">Comment</th>
                  <th className="px-6 py-3 font-semibold">Status</th>
                  <th className="px-6 py-3 font-semibold text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {reviews.map((r) => (
                  <tr key={r.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-6 py-4 font-mono text-xs text-indigo-600">#{r.rideId}</td>
                    <td className="px-6 py-4 font-medium text-gray-900">{r.riderName}</td>
                    <td className="px-6 py-4 text-gray-600">{r.driverName}</td>
                    <td className="px-6 py-4">
                      <div className="flex items-center text-amber-500 text-sm">
                        {'★'.repeat(r.stars)}
                        <span className="text-gray-300">{'★'.repeat(5 - r.stars)}</span>
                        <span className="text-xs text-gray-600 font-bold ml-1.5">{r.stars}.0</span>
                      </div>
                    </td>
                    <td className="px-6 py-4 max-w-sm">
                      {r.comment ? (
                        <p className="text-xs text-gray-700 italic">"{r.comment}"</p>
                      ) : (
                        <span className="text-xs text-gray-400">No text comment provided</span>
                      )}
                    </td>
                    <td className="px-6 py-4">
                      <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold ${
                        r.hidden ? 'bg-rose-100 text-rose-800' : 'bg-emerald-100 text-emerald-800'
                      }`}>
                        {r.hidden ? 'HIDDEN' : 'VISIBLE'}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right space-x-2 whitespace-nowrap">
                      <button
                        onClick={() => toggleVisibilityMutation.mutate({ id: r.id, hide: !r.hidden })}
                        className={`text-xs font-medium px-2.5 py-1 rounded transition ${
                          r.hidden
                            ? 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100'
                            : 'bg-amber-50 text-amber-700 hover:bg-amber-100'
                        }`}
                      >
                        {r.hidden ? 'Unhide' : 'Hide from Public'}
                      </button>
                      <button
                        onClick={() => {
                          if (confirm('Permanently delete this review record?')) {
                            deleteMutation.mutate(r.id);
                          }
                        }}
                        className="text-xs font-medium text-rose-600 hover:text-rose-800 bg-rose-50 hover:bg-rose-100 px-2.5 py-1 rounded transition"
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
    </div>
  );
}
