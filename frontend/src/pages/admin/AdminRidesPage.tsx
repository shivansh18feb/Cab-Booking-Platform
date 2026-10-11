import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { adminService } from '../../services/adminService';
import { formatCurrency } from '../../utils/formatters';

const statusColor: Record<string, string> = {
  REQUESTED:   'bg-yellow-100 text-yellow-700',
  ACCEPTED:    'bg-blue-100 text-blue-700',
  IN_PROGRESS: 'bg-indigo-100 text-indigo-700',
  COMPLETED:   'bg-green-100 text-green-700',
  CANCELLED:   'bg-red-100 text-red-700',
};

export default function AdminRidesPage() {
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: ['adminRides', page, statusFilter],
    queryFn: () => adminService.getAllRides(page, 20, statusFilter || undefined),
  });

  return (
    <div className="max-w-6xl mx-auto px-4 py-8 fade-in">
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <Link to="/admin/dashboard" className="text-gray-400 hover:text-gray-600">← Dashboard</Link>
          <h1 className="page-title">All Rides</h1>
        </div>
        <select
          value={statusFilter}
          onChange={e => { setStatusFilter(e.target.value); setPage(0); }}
          className="input-field w-44 text-sm"
        >
          <option value="">All statuses</option>
          {['REQUESTED', 'ACCEPTED', 'DRIVER_ARRIVING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'].map(s => (
            <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>
          ))}
        </select>
      </div>

      {isLoading ? (
        <div className="skeleton h-64 rounded-xl" />
      ) : (
        <>
          <div className="card overflow-hidden p-0">
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="bg-gray-50 border-b border-gray-100">
                  <tr>
                    {['Ride ID', 'Pickup', 'Dropoff', 'Status', 'Vehicle', 'Fare', 'Date'].map(h => (
                      <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide">{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {data?.content?.map((ride: any) => (
                    <tr key={ride.id} className="hover:bg-gray-50 transition-colors">
                      <td className="px-4 py-3 font-mono text-xs text-gray-500">{ride.id.slice(0, 8)}…</td>
                      <td className="px-4 py-3 text-gray-700 max-w-[180px] truncate">{ride.pickupAddress}</td>
                      <td className="px-4 py-3 text-gray-700 max-w-[180px] truncate">{ride.dropoffAddress}</td>
                      <td className="px-4 py-3">
                        <span className={`status-badge ${statusColor[ride.status] ?? 'bg-gray-100 text-gray-600'}`}>
                          {ride.status.replace(/_/g, ' ')}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-gray-600">{ride.vehicleCategory}</td>
                      <td className="px-4 py-3 font-medium">{formatCurrency(ride.finalFare ?? ride.estimatedFare ?? 0, ride.currency)}</td>
                      <td className="px-4 py-3 text-gray-400">{new Date(ride.requestedAt).toLocaleDateString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {(data?.totalPages ?? 1) > 1 && (
            <div className="flex items-center justify-center gap-3 mt-4">
              <button disabled={page === 0} onClick={() => setPage(p => p - 1)} className="btn-secondary text-sm px-3 py-1.5">← Prev</button>
              <span className="text-sm text-gray-500">Page {page + 1} of {data?.totalPages}</span>
              <button disabled={page + 1 >= (data?.totalPages ?? 1)} onClick={() => setPage(p => p + 1)} className="btn-secondary text-sm px-3 py-1.5">Next →</button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
