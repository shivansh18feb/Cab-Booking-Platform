import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import apiClient from '../../services/api';

export default function PromotionsPage() {
  const [code, setCode] = useState('');
  const [result, setResult] = useState<{ valid: boolean; discount?: number; message: string } | null>(null);

  const validateMutation = useMutation({
    mutationFn: async () => {
      const res = await apiClient.post('/promos/validate', { code });
      return res.data.data;
    },
    onSuccess: (data) => setResult({ valid: true, discount: data.discountAmount, message: data.message ?? 'Promo code is valid!' }),
    onError: (err: any) => setResult({ valid: false, message: err.response?.data?.message ?? 'Invalid or expired promo code.' }),
  });

  const promos = [
    { code: 'WELCOME20', title: '20% Off First Ride', desc: 'Valid for new riders on their first booking.', icon: '🎉' },
    { code: 'FLAT5OFF',  title: '\$5 Flat Discount',   desc: 'Get \$5 off any ride over \$10.',              icon: '💵' },
    { code: 'VIPRIDE',   title: 'VIP Ride Upgrade',    desc: 'Free upgrade to Premium on select routes.',    icon: '👑' },
  ];

  return (
    <div className="max-w-2xl mx-auto px-4 py-8 fade-in">
      <h1 className="page-title mb-6">Promotions & Offers</h1>

      {/* Promo Validator */}
      <div className="card mb-6">
        <h2 className="section-title mb-3">Validate a Promo Code</h2>
        <div className="flex gap-2">
          <input
            value={code}
            onChange={e => setCode(e.target.value.toUpperCase())}
            placeholder="Enter promo code"
            className="input-field flex-1 uppercase"
          />
          <button
            onClick={() => validateMutation.mutate()}
            disabled={!code || validateMutation.isPending}
            className="btn-primary px-5"
          >
            {validateMutation.isPending ? '…' : 'Check'}
          </button>
        </div>
        {result && (
          <div className={`mt-3 p-3 rounded-lg text-sm ${result.valid ? 'bg-green-50 text-green-700 border border-green-200' : 'bg-red-50 text-red-700 border border-red-200'}`}>
            {result.valid ? '✅' : '❌'} {result.message}
            {result.valid && result.discount != null && (
              <span className="ml-2 font-semibold">Saves: \${result.discount.toFixed(2)}</span>
            )}
          </div>
        )}
      </div>

      {/* Available Promos */}
      <h2 className="section-title mb-3">Available Offers</h2>
      <div className="space-y-3">
        {promos.map(p => (
          <div key={p.code} className="card-hover flex items-center gap-4">
            <div className="text-4xl">{p.icon}</div>
            <div className="flex-1">
              <p className="font-semibold text-gray-900">{p.title}</p>
              <p className="text-sm text-gray-500">{p.desc}</p>
            </div>
            <button
              onClick={() => { setCode(p.code); }}
              className="text-xs font-mono bg-indigo-50 text-indigo-700 px-3 py-1.5 rounded-lg border border-indigo-200 hover:bg-indigo-100 transition-colors shrink-0"
            >
              {p.code}
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
