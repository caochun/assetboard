const colorMap: Record<string, string> = {
  CRITICAL: 'bg-red-100 text-red-800',
  MAJOR: 'bg-orange-100 text-orange-800',
  MINOR: 'bg-yellow-100 text-yellow-800',
  WARNING: 'bg-amber-100 text-amber-800',
  INDETERMINATE: 'bg-gray-100 text-gray-800',
  IN_LEASE: 'bg-green-100 text-green-800',
  RETURNED: 'bg-gray-100 text-gray-800',
  DISPOSED: 'bg-red-100 text-red-800',
  ACTIVE: 'bg-green-100 text-green-800',
  EXPIRED: 'bg-gray-100 text-gray-800',
  TERMINATED: 'bg-red-100 text-red-800',
};

export default function StatusBadge({ value }: { value: string }) {
  const color = colorMap[value] || 'bg-blue-100 text-blue-800';
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${color}`}>
      {value}
    </span>
  );
}
