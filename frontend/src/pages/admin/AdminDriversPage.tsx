import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { adminService } from '../../services/adminService';

const verificationColor: Record<string, string> = {
  PENDING:  'bg-yellow-100 text-yellow-700',
  APPROVED: 'bg-green-100 text-green-700',
  REJECTED: 'bg-red-100 text-red-700',
};

export default function AdminDriversPage() {
  const qc = useQueryClient();

  const { data: pending, isLoading: loadingPending } = useQuery({
    queryKey: ['pendingDrivers'],
    queryFn: adminService.getPendingDrivers,
  });

  const verifyMutation = useMutation({
    mutationFn: ({ driverId, approved }: { driverId: string; approved: boolean }) =>
      adminService.verifyDriver(driverId, approved),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['pendingDrivers'] }),
  });

  return (
    <div className="max-w-5xl mx-auto px-4 py-8 fade-in">
      <div className="flex items-center gap-3 mb-6">
        <Link to="/admin/dashboard" className="text-gray-400 hover:text-gray-600">← Dashboard</Link>
        <h1 className="page-title">Manage Drivers</h1>
      </div>

      {/* Pending Verification */}
      <div className="mb-8">
        <h2 className="section-title mb-4">Pending Verification ({pending?.length ?? 0})</h2>
        {loadingPending ? (
          <div className="space-y-3">{[...Array(3)].map((_, i) => <div key={i} className="skeleton h-24 rounded-xl" />)}</div>
        ) : !pending || pending.length === 0 ? (
          <div className="card text-center py-8 text-gray-400">
            <div className="text-3xl mb-2">✅</div>
            <p>No pending driver verifications.</p>
          </div>
        ) : (
          <div className="space-y-3">
            {pending.map((driver: any) => (
              <div key={driver.id} className="card flex items-center justify-between gap-4">
                <div>
                  <p className="font-semibold text-gray-900">{driver.user?.fullName ?? 'Unknown'}</p>
                  <p className="text-sm text-gray-500">{driver.user?.email}</p>
                  <div className="flex gap-2 mt-1 text-xs text-gray-400">
                    <span>License: {driver.licenseNumber}</span>
                    <span>·</span>
                    <span>{driver.vehicle?.make} {driver.vehicle?.model} {driver.vehicle?.year}</span>
                  </div>
                </div>
                <div className="flex items-center gap-2 shrink-0">
                  <span className={`status-badge ${verificationColor[driver.verificationStatus] ?? ''}`}>
                    {driver.verificationStatus}
                  </span>
                  <button
                    onClick={() => verifyMutation.mutate({ driverId: driver.id, approved: true })}
                    className="text-xs bg-green-600 text-white px-3 py-1.5 rounded-lg hover:bg-green-700 transition-colors"
                  >
                    Approve
                  </button>
                  <button
                    onClick={() => verifyMutation.mutate({ driverId: driver.id, approved: false })}
                    className="text-xs bg-red-600 text-white px-3 py-1.5 rounded-lg hover:bg-red-700 transition-colors"
                  >
                    Reject
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
