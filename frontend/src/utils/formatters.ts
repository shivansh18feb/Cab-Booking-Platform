export function formatCurrency(
  amount: number | null | undefined,
  currency: string = 'INR'
): string {
  const val = amount != null && !isNaN(Number(amount)) ? Number(amount) : 0;
  const symbol = currency === 'USD' ? '$' : '₹';
  return `${symbol}${val.toFixed(2)}`;
}
