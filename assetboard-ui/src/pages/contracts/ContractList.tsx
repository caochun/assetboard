import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getContracts, saveContract, deleteContract } from '../../api/contract';
import { useAuth } from '../../hooks/useAuth';
import { usePagination } from '../../hooks/usePagination';
import PageHeader from '../../components/PageHeader';
import DataTable from '../../components/DataTable';
import StatusBadge from '../../components/StatusBadge';
import type { Contract, PageData } from '../../types';
import dayjs from 'dayjs';

export default function ContractList() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { page, pageSize, goToPage } = usePagination();
  const [data, setData] = useState<PageData<Contract>>({ data: [], totalPages: 0, totalElements: 0, hasNext: false });
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ contractNo: '', amount: '', currency: 'CNY', lessor: '', lessee: '', status: 'ACTIVE' });

  const load = () => {
    if (!user) return;
    getContracts(user.tenantId, page, pageSize).then((res) => setData(res.data));
  };

  useEffect(load, [user, page, pageSize]);

  const handleSave = async () => {
    await saveContract({ ...form, amount: form.amount ? Number(form.amount) : undefined, signDate: Date.now() });
    setShowForm(false);
    setForm({ contractNo: '', amount: '', currency: 'CNY', lessor: '', lessee: '', status: 'ACTIVE' });
    load();
  };

  const handleDelete = async (id: string) => {
    await deleteContract(id);
    load();
  };

  const columns = [
    { key: 'contractNo', title: '合同编号' },
    { key: 'amount', title: '金额', render: (r: Contract) => r.amount?.toLocaleString() ?? '-' },
    { key: 'currency', title: '币种' },
    { key: 'lessor', title: '出租方' },
    { key: 'lessee', title: '承租方' },
    { key: 'status', title: '状态', render: (r: Contract) => <StatusBadge value={r.status} /> },
    { key: 'signDate', title: '签约日期', render: (r: Contract) => r.signDate ? dayjs(r.signDate).format('YYYY-MM-DD') : '-' },
    { key: 'actions', title: '操作', render: (r: Contract) => (
      <button onClick={(e) => { e.stopPropagation(); handleDelete(r.id); }} className="text-red-600 hover:underline text-xs">删除</button>
    )},
  ];

  return (
    <div>
      <PageHeader
        title="合同管理"
        action={<button onClick={() => setShowForm(true)} className="rounded-lg bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-700">新增合同</button>}
      />
      {showForm && (
        <div className="bg-white rounded-xl border border-gray-200 p-5 mb-5 space-y-3">
          <input placeholder="合同编号" value={form.contractNo} onChange={(e) => setForm({ ...form, contractNo: e.target.value })} className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3.5 py-2.5 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent focus:bg-white transition-colors" />
          <input placeholder="金额" type="number" value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })} className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3.5 py-2.5 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent focus:bg-white transition-colors" />
          <input placeholder="出租方" value={form.lessor} onChange={(e) => setForm({ ...form, lessor: e.target.value })} className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3.5 py-2.5 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent focus:bg-white transition-colors" />
          <input placeholder="承租方" value={form.lessee} onChange={(e) => setForm({ ...form, lessee: e.target.value })} className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3.5 py-2.5 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent focus:bg-white transition-colors" />
          <div className="flex gap-2">
            <button onClick={handleSave} className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700 transition-colors">保存</button>
            <button onClick={() => setShowForm(false)} className="rounded-lg border border-gray-200 px-4 py-2 text-sm font-medium text-gray-600 hover:bg-gray-50 transition-colors">取消</button>
          </div>
        </div>
      )}
      <DataTable columns={columns} data={data.data} page={page} totalPages={data.totalPages} onPageChange={goToPage} onRowClick={(r) => navigate(`/contracts/${r.id}`)} />
    </div>
  );
}
