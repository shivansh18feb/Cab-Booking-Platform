import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from '../../services/api';

interface SavedPlace {
  id: string;
  label: string;
  address: string;
  lat: number;
  lng: number;
}

export default function SavedPlacesPage() {
  const qc = useQueryClient();
  const [showForm, setShowForm] = useState(false);
  const [label, setLabel] = useState('');
  const [address, setAddress] = useState('');

  const { data: places = [], isLoading } = useQuery<SavedPlace[]>({
    queryKey: ['savedPlaces'],
    queryFn: async () => (await apiClient.get('/riders/saved-places')).data.data,
  });

  const addMutation = useMutation({
    mutationFn: () => apiClient.post('/riders/saved-places', { label, address, lat: 0, lng: 0 }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['savedPlaces'] });
      setLabel(''); setAddress(''); setShowForm(false);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (placeId: string) => apiClient.delete(`/riders/saved-places/${placeId}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['savedPlaces'] }),
  });

  return (
    <div className="max-w-2xl mx-auto px-4 py-8 fade-in">
      <div className="flex items-center justify-between mb-6">
        <h1 className="page-title">Saved Places</h1>
        <button onClick={() => setShowForm(v => !v)} className="btn-primary text-sm">
          {showForm ? 'Cancel' : '+ Add Place'}
        </button>
      </div>

      {showForm && (
        <div className="card mb-4 border border-indigo-200">
          <h3 className="font-semibold mb-3">Add New Place</h3>
          <div className="space-y-3">
            <div>
              <label className="label-field">Label</label>
              <input value={label} onChange={e => setLabel(e.target.value)} placeholder="Home, Work, Gym…" className="input-field" />
            </div>
            <div>
              <label className="label-field">Address</label>
              <input value={address} onChange={e => setAddress(e.target.value)} placeholder="Full address" className="input-field" />
            </div>
            <button onClick={() => addMutation.mutate()} disabled={!label || !address || addMutation.isPending} className="btn-primary w-full">
              {addMutation.isPending ? 'Saving…' : 'Save Place'}
            </button>
          </div>
        </div>
      )}

      {isLoading ? (
        <div className="space-y-3">{[...Array(3)].map((_, i) => <div key={i} className="skeleton h-16 rounded-xl" />)}</div>
      ) : places.length === 0 ? (
        <div className="card text-center py-12">
          <div className="text-4xl mb-2">📍</div>
          <p className="text-gray-500">No saved places yet. Add your home or work for quick booking.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {places.map(place => (
            <div key={place.id} className="card flex items-center justify-between gap-4">
              <div className="flex items-center gap-3">
                <span className="w-10 h-10 bg-indigo-100 rounded-xl flex items-center justify-center text-xl">
                  {place.label.toLowerCase().includes('home') ? '🏠' :
                   place.label.toLowerCase().includes('work') ? '💼' : '📍'}
                </span>
                <div>
                  <p className="font-semibold text-gray-900">{place.label}</p>
                  <p className="text-sm text-gray-500">{place.address}</p>
                </div>
              </div>
              <button
                onClick={() => deleteMutation.mutate(place.id)}
                className="text-gray-400 hover:text-red-500 transition-colors p-2"
                title="Delete"
              >
                🗑️
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
