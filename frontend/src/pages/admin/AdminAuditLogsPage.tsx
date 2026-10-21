import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { adminService } from '../../services/adminService';
import { AuditLogEntry } from '../../types/admin';

export default function AdminAuditLogsPage() {
  const [page, setPage] = useState(0);
  const [actionFilter, setActionFilter] = useState('');
  const [resourceTypeFilter, setResourceTypeFilter] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: ['adminAuditLogs', page, actionFilter, resourceTypeFilter],
    queryFn: () => adminService.getAuditLogs(page, 15, actionFilter || undefined, resourceTypeFilter || undefined),
  });

  const logs = (data?.content || []) as AuditLogEntry[];
  const totalPages = data?.totalPages || 0;

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-6 border-b border-gray-200 mb-6 gap-4">
        <div>
          <div className="flex items-center gap-2 text-sm text-gray-500 mb-1">
            <Link to="/admin/dashboard" className="hover:text-indigo-600">Admin</Link>
            <span>/</span>
            <span className="text-gray-900 font-medium">Audit Logs</span>
          </div>
          <h1 className="text-2xl font-bold text-gray-900">Security & Operational Audit Logs</h1>
          <p className="text-sm text-gray-500 mt-0.5">Immutable audit trail of administrator actions, promotions, verifications, and status changes</p>
        </div>

        {/* Filters */}
        <div className="flex flex-wrap items-center gap-3">
          <input
            type="text"
            placeholder="Filter by action (e.g. UPDATE)"
            value={actionFilter}
            onChange={(e) => { setActionFilter(e.target.value); setPage(0); }}
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm bg-white"
          />

          <input
            type="text"
            placeholder="Filter by resource (e.g. USER, PROMO)"
            value={resourceTypeFilter}
            onChange={(e) => { setResourceTypeFilter(e.target.value); setPage(0); }}
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm bg-white"
          />
        </div>
      </div>

      {/* Audit Log Table */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        {isLoading ? (
          <div className="p-8 text-center text-gray-500">
            <div className="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin mx-auto mb-2" />
            Loading audit logs...
          </div>
        ) : logs.length === 0 ? (
          <div className="p-12 text-center text-gray-500">
            <p className="text-lg font-medium text-gray-700">No audit records found</p>
            <p className="text-sm mt-1">Actions performed by admins will be recorded and shown here in real time.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-gray-50 border-b border-gray-200 text-gray-600 uppercase text-xs">
                <tr>
                  <th className="px-6 py-3 font-semibold">Timestamp</th>
                  <th className="px-6 py-3 font-semibold">Actor</th>
                  <th className="px-6 py-3 font-semibold">Action</th>
                  <th className="px-6 py-3 font-semibold">Resource</th>
                  <th className="px-6 py-3 font-semibold">Details</th>
                  <th className="px-6 py-3 font-semibold">IP Address</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 font-mono text-xs">
                {logs.map((log) => (
                  <tr key={log.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-6 py-4 text-gray-500 whitespace-nowrap">
                      {new Date(log.createdAt).toLocaleString()}
                    </td>
                    <td className="px-6 py-4">
                      <div className="font-sans font-medium text-gray-900">{log.actorEmail || 'System'}</div>
                      {log.actorRole && (
                        <span className="text-[10px] uppercase font-bold text-indigo-600 bg-indigo-50 px-1.5 py-0.5 rounded">
                          {log.actorRole}
                        </span>
                      )}
                    </td>
                    <td className="px-6 py-4">
                      <span className="font-bold text-purple-700 bg-purple-50 px-2 py-0.5 rounded border border-purple-200">
                        {log.action}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <span className="font-semibold text-gray-800">{log.resourceType}</span>
                      {log.resourceId && (
                        <span className="text-gray-500 ml-1">#{log.resourceId}</span>
                      )}
                    </td>
                    <td className="px-6 py-4 font-sans text-gray-600 max-w-xs truncate">
                      {log.details || '—'}
                    </td>
                    <td className="px-6 py-4 text-gray-400">
                      {log.ipAddress || '—'}
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
                className="px-3 py-1 text-xs font-medium border border-gray-300 rounded bg-white disabled:opacity-50 hover:bg-gray-100 font-sans"
              >
                Previous
              </button>
              <button
                disabled={page >= totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
                className="px-3 py-1 text-xs font-medium border border-gray-300 rounded bg-white disabled:opacity-50 hover:bg-gray-100 font-sans"
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
