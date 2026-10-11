import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import apiClient from '../services/api';
import { useAuth } from '../context/AuthContext';

const schema = z.object({
  subject: z.string().min(5, 'Subject too short'),
  message: z.string().min(20, 'Please provide more detail (at least 20 chars)'),
  priority: z.enum(['LOW', 'MEDIUM', 'HIGH', 'URGENT']),
});
type FormData = z.infer<typeof schema>;

const statusColor: Record<string, string> = {
  OPEN:        'bg-blue-100 text-blue-700',
  IN_PROGRESS: 'bg-yellow-100 text-yellow-700',
  RESOLVED:    'bg-green-100 text-green-700',
  CLOSED:      'bg-gray-100 text-gray-600',
};

export default function SupportPage() {
  const { user } = useAuth();
  const qc = useQueryClient();
  const [showForm, setShowForm] = useState(false);

  const { data: tickets = [], isLoading } = useQuery<any[]>({
    queryKey: ['supportTickets'],
    queryFn: async () => (await apiClient.get('/support/my')).data.data ?? [],
    enabled: !!user,
  });

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { priority: 'MEDIUM' },
  });

  const createMutation = useMutation({
    mutationFn: (data: FormData) => apiClient.post('/support', data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['supportTickets'] });
      reset();
      setShowForm(false);
    },
  });

  return (
    <div className="max-w-3xl mx-auto px-4 py-8 fade-in">
      <div className="flex items-center justify-between mb-6">
        <h1 className="page-title">Support</h1>
        {user && (
          <button onClick={() => setShowForm(v => !v)} className="btn-primary text-sm">
            {showForm ? 'Cancel' : '+ New Ticket'}
          </button>
        )}
      </div>

      {!user && (
        <div className="card text-center py-10">
          <p className="text-gray-500 mb-4">Please log in to submit or view support tickets.</p>
          <a href="/login" className="btn-primary">Sign In</a>
        </div>
      )}

      {user && showForm && (
        <form onSubmit={handleSubmit(d => createMutation.mutate(d))} className="card mb-6 border border-indigo-200 space-y-4">
          <h2 className="section-title">New Support Ticket</h2>
          <div>
            <label className="label-field">Priority</label>
            <select {...register('priority')} className="input-field">
              {['LOW', 'MEDIUM', 'HIGH', 'URGENT'].map(p => <option key={p}>{p}</option>)}
            </select>
          </div>
          <div>
            <label className="label-field">Subject</label>
            <input {...register('subject')} className={`input-field ${errors.subject ? 'input-error' : ''}`} placeholder="Brief summary of your issue" />
            {errors.subject && <p className="text-xs text-red-500 mt-1">{errors.subject.message}</p>}
          </div>
          <div>
            <label className="label-field">Message</label>
            <textarea {...register('message')} rows={4} className={`input-field resize-none ${errors.message ? 'input-error' : ''}`} placeholder="Describe your issue in detail…" />
            {errors.message && <p className="text-xs text-red-500 mt-1">{errors.message.message}</p>}
          </div>
          <button type="submit" disabled={isSubmitting} className="btn-primary w-full">
            {isSubmitting ? 'Submitting…' : 'Submit Ticket'}
          </button>
        </form>
      )}

      {user && (
        <div>
          <h2 className="section-title mb-3">My Tickets</h2>
          {isLoading ? (
            <div className="space-y-3">{[...Array(3)].map((_, i) => <div key={i} className="skeleton h-16 rounded-xl" />)}</div>
          ) : tickets.length === 0 ? (
            <div className="card text-center py-10 text-gray-400">
              <div className="text-4xl mb-2">🎫</div>
              <p>No support tickets yet.</p>
            </div>
          ) : (
            <div className="space-y-3">
              {tickets.map((t: any) => (
                <div key={t.id} className="card flex items-start justify-between gap-4">
                  <div>
                    <p className="font-semibold text-gray-900">{t.subject}</p>
                    <p className="text-sm text-gray-500 mt-0.5">{t.message?.slice(0, 80)}…</p>
                    <p className="text-xs text-gray-400 mt-1">{new Date(t.createdAt).toLocaleDateString()}</p>
                  </div>
                  <div className="flex flex-col items-end gap-1 shrink-0">
                    <span className={`status-badge ${statusColor[t.status] ?? ''}`}>{t.status}</span>
                    <span className="text-xs text-gray-400">{t.priority}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
