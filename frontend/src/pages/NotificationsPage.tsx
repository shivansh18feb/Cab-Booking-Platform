import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../services/api';
import { Bell, CheckCheck } from 'lucide-react';

interface NotificationItem {
  id: number;
  title: string;
  message: string;
  notificationType: string;
  rideId?: number;
  read: boolean;
  createdAt: string;
}

export default function NotificationsPage() {
  const qc = useQueryClient();
  const [page, setPage] = useState(0);

  const { data, isLoading } = useQuery({
    queryKey: ['notifications', page],
    queryFn: async () => {
      const res = await apiClient.get(`/notifications?page=${page}&size=20`);
      return res.data.data as { content: NotificationItem[]; totalPages: number; totalElements: number; number: number };
    },
  });

  const markAllMutation = useMutation({
    mutationFn: async () => {
      await apiClient.patch('/notifications/read-all');
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['notifications'] });
    },
  });

  const typeColors: Record<string, string> = {
    RIDE: 'bg-blue-100 text-blue-700',
    PAYMENT: 'bg-green-100 text-green-700',
    SYSTEM: 'bg-gray-100 text-gray-700',
    SOS: 'bg-red-100 text-red-700',
    PROMO: 'bg-purple-100 text-purple-700',
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-2xl mx-auto px-4 py-8">
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-3">
            <Bell className="h-6 w-6 text-indigo-600" />
            <h1 className="text-2xl font-bold text-gray-900">Notifications</h1>
            {data?.totalElements != null && (
              <span className="text-sm text-gray-500">({data.totalElements} total)</span>
            )}
          </div>
          <button
            onClick={() => markAllMutation.mutate()}
            disabled={markAllMutation.isPending}
            className="flex items-center gap-1.5 text-sm text-indigo-600 hover:text-indigo-800 font-medium disabled:opacity-50"
          >
            <CheckCheck className="h-4 w-4" />
            Mark all read
          </button>
        </div>

        {isLoading ? (
          <div className="flex justify-center py-16">
            <div className="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin" />
          </div>
        ) : data?.content.length === 0 ? (
          <div className="text-center py-16 text-gray-400">
            <Bell className="h-12 w-12 mx-auto mb-3 opacity-30" />
            <p className="font-medium">No notifications yet</p>
            <p className="text-sm">We'll let you know when something happens</p>
          </div>
        ) : (
          <div className="space-y-2">
            {data?.content.map((notif) => (
              <div
                key={notif.id}
                className={`bg-white rounded-xl p-4 border transition ${
                  notif.read ? 'border-gray-100 opacity-75' : 'border-indigo-200 shadow-sm'
                }`}
              >
                <div className="flex items-start gap-3">
                  {!notif.read && (
                    <div className="w-2 h-2 rounded-full bg-indigo-500 mt-2 shrink-0" />
                  )}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap mb-1">
                      <p className={`text-sm font-semibold ${notif.read ? 'text-gray-600' : 'text-gray-900'}`}>
                        {notif.title}
                      </p>
                      <span className={`text-[10px] font-bold uppercase px-1.5 py-0.5 rounded ${typeColors[notif.notificationType] ?? typeColors['SYSTEM']}`}>
                        {notif.notificationType}
                      </span>
                    </div>
                    <p className="text-sm text-gray-600">{notif.message}</p>
                    <p className="text-xs text-gray-400 mt-1">
                      {new Date(notif.createdAt).toLocaleString()}
                      {notif.rideId && (
                        <span className="ml-2 text-indigo-500">· Ride #{notif.rideId}</span>
                      )}
                    </p>
                  </div>
                </div>
              </div>
            ))}

            {/* Pagination */}
            {data && data.totalPages > 1 && (
              <div className="flex justify-center gap-3 pt-4">
                <button
                  onClick={() => setPage(p => Math.max(0, p - 1))}
                  disabled={page === 0}
                  className="px-4 py-2 text-sm bg-white border rounded-lg disabled:opacity-40 hover:bg-gray-50"
                >
                  ← Previous
                </button>
                <span className="px-4 py-2 text-sm text-gray-500">
                  Page {page + 1} of {data.totalPages}
                </span>
                <button
                  onClick={() => setPage(p => p + 1)}
                  disabled={page >= data.totalPages - 1}
                  className="px-4 py-2 text-sm bg-white border rounded-lg disabled:opacity-40 hover:bg-gray-50"
                >
                  Next →
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
