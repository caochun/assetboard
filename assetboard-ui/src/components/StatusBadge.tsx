const colorMap: Record<string, string> = {
  CRITICAL: 'bg-red-100 text-red-800',
  MAJOR: 'bg-orange-100 text-orange-800',
  MINOR: 'bg-yellow-100 text-yellow-800',
  WARNING: 'bg-amber-100 text-amber-800',
  INDETERMINATE: 'bg-gray-100 text-gray-800',
  IN_LEASE: 'bg-green-100 text-green-800',
  REPURCHASED: 'bg-blue-100 text-blue-800',
  RETURNED: 'bg-gray-100 text-gray-800',
  DISPOSED: 'bg-red-100 text-red-800',
  ACTIVE: 'bg-green-100 text-green-800',
  EXPIRED: 'bg-gray-100 text-gray-800',
  TERMINATED: 'bg-red-100 text-red-800',
};

const labelMap: Record<string, string> = {
  CRITICAL: '严重',
  MAJOR: '重要',
  MINOR: '次要',
  WARNING: '警告',
  INDETERMINATE: '未定',
  IN_LEASE: '在租',
  REPURCHASED: '已回购',
  RETURNED: '已退还',
  DISPOSED: '已处置',
  ACTIVE: '生效中',
  EXPIRED: '已到期',
  TERMINATED: '已终止',
};

export default function StatusBadge({ value }: { value: string }) {
  const color = colorMap[value] || 'bg-blue-100 text-blue-800';
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${color}`}>
      {labelMap[value] || value}
    </span>
  );
}
