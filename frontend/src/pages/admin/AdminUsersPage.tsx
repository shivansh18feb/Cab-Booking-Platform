import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { adminService } from '../../services/adminService';

const statusColor: Record<string, string> = {
  ACTIVE:    'bg-green-100 text-green-700',
  SUSPENDED: 'bg-red-100 text-red-700',
  PENDING:   'bg-yellow-100 text-yellow-700',
};

export default function AdminUsersPage() {
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: ['adminUsers', page, search],
    queryFn: () => adminService.getUsers(page, 20, search),
  });

  const statusMutation = useMutation({
    mutationFn: ({ userId, status }: { userId: string; status: string }) =>
      adminService.updateUserStatus(userId, status),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['adminUsers'] }),
  });

  return (
    <div className="max-w-6xl mx-auto px-4 py-8 fade-in">
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <Link to="/admin/dashboard" className="text-gray-400 hover:text-gray-600">← Dashboard</Link>
          <h1 className="page-title">Manage Users</h1>
        </div>
        <div className="flex gap-2">
          <input
            value={search}
            onChange={e => { setSearch(e.target.value); setPage(0); }}
            placeholder="Search by email or name…"
            className="input-field w-56 text-sm"
          />
        </div>
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
                    {['Name', 'Email', 'Role', 'Status', 'Joined', 'Actions'].map(h => (
                      <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide">{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {data?.content?.map((user: any) => (
                    <tr key={user.id} className="hover:bg-gray-50 transition-colors">
                      <td className="px-4 py-3 font-medium text-gray-900">{user.fullName}</td>
                      <td className="px-4 py-3 text-gray-600">{user.email}</td>
                      <td className="px-4 py-3">
                        <span className={`status-badge ${user.role === 'ADMIN' ? 'bg-purple-100 text-purple-700' : user.role === 'DRIVER' ? 'bg-blue-100 text-blue-700' : 'bg-gray-100 text-gray-700'}`}>
                          {user.role}
                        </span>
                      </td>
                      <td className="px-4 py-3">
                        <span className={`status-badge ${statusColor[user.accountStatus] ?? 'bg-gray-100 text-gray-600'}`}>
                          {user.accountStatus}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-gray-400">{new Date(user.createdAt).toLocaleDateString()}</td>
                      <td className="px-4 py-3">
                        {user.accountStatus === 'ACTIVE' ? (
                          <button
                            onClick={() => statusMutation.mutate({ userId: user.id, status: 'SUSPENDED' })}
                            className="text-xs text-red-600 hover:underline"
                          >
                            Suspend
                          </button>
                        ) : user.accountStatus === 'SUSPENDED' ? (
                          <button
                            onClick={() => statusMutation.mutate({ userId: user.id, status: 'ACTIVE' })}
                            className="text-xs text-green-600 hover:underline"
                          >
                            Reactivate
                          </button>
                        ) : null}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Pagination */}
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
