import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getCustomers, saveCustomer, deleteCustomer } from '../../api/customer';
import { useAuth } from '../../hooks/useAuth';
import { usePagination } from '../../hooks/usePagination';
import PageHeader from '../../components/PageHeader';
import DataTable from '../../components/DataTable';
import type { Customer, PageData } from '../../types';

export default function CustomerList() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { page, pageSize, goToPage } = usePagination();
  const [data, setData] = useState<PageData<Customer>>({ data: [], totalPages: 0, totalElements: 0, hasNext: false });
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ name: '', address: '', creditAmount: '' });

  const load = () => {
    if (!user) return;
    getCustomers(user.tenantId, page, pageSize).then((res) => setData(res.data));
  };

  useEffect(load, [user, page, pageSize]);

  const handleSave = async () => {
    if (!user) return;
    await saveCustomer({ tenantId: user.tenantId, name: form.name, address: form.address, creditAmount: form.creditAmount ? Number(form.creditAmount) : undefined });
    setShowForm(false);
    setForm({ name: '', address: '', creditAmount: '' });
    load();
  };

  const handleDelete = async (id: string) => {
    await deleteCustomer(id);
    load();
  };

  const columns = [
    { key: 'name', title: '名称' },
    { key: 'address', title: '地址' },
    { key: 'creditAmount', title: '授信额度', render: (r: Customer) => r.creditAmount?.toLocaleString() ?? '-' },
    { key: 'remainingPrincipal', title: '剩余本金', render: (r: Customer) => r.remainingPrincipal?.toLocaleString() ?? '-' },
    { key: 'actions', title: '操作', render: (r: Customer) => (
      <button onClick={(e) => { e.stopPropagation(); handleDelete(r.id); }} className="text-red-600 hover:underline text-xs">删除</button>
    )},
  ];

  return (
    <div>
      <PageHeader
        title="客户管理"
        action={<button onClick={() => setShowForm(true)} className="rounded-lg bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-700">新增客户</button>}
      />
      {showForm && (
        <div className="bg-white rounded-xl border border-gray-200 p-5 mb-5 space-y-3">
          <input placeholder="客户名称" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3.5 py-2.5 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent focus:bg-white transition-colors" />
          <input placeholder="地址" value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3.5 py-2.5 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent focus:bg-white transition-colors" />
          <input placeholder="授信额度" type="number" value={form.creditAmount} onChange={(e) => setForm({ ...form, creditAmount: e.target.value })} className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3.5 py-2.5 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent focus:bg-white transition-colors" />
          <div className="flex gap-2">
            <button onClick={handleSave} className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700 transition-colors">保存</button>
            <button onClick={() => setShowForm(false)} className="rounded-lg border border-gray-200 px-4 py-2 text-sm font-medium text-gray-600 hover:bg-gray-50 transition-colors">取消</button>
          </div>
        </div>
      )}
      <DataTable columns={columns} data={data.data} page={page} totalPages={data.totalPages} onPageChange={goToPage} onRowClick={(r) => navigate(`/customers/${r.id}`)} />
    </div>
  );
}
