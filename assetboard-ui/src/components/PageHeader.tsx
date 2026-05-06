export default function PageHeader({ title, action }: { title: string; action?: React.ReactNode }) {
  return (
    <div className="flex items-center justify-between mb-8">
      <h1 className="text-xl font-semibold text-gray-900">{title}</h1>
      {action && <div>{action}</div>}
    </div>
  );
}
