import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getProjectById } from '../../api/project';
import { getCustomerById } from '../../api/customer';
import { getContracts } from '../../api/contract';
import StatusBadge from '../../components/StatusBadge';
import type { Project, Customer, Contract } from '../../types';
import dayjs from 'dayjs';

export default function ProjectDetail() {
  const { id } = useParams<{ id: string }>();
  const [project, setProject] = useState<Project | null>(null);
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [contracts, setContracts] = useState<Contract[]>([]);

  useEffect(() => {
    if (!id) return;
    getProjectById(id).then((r) => {
      setProject(r.data);
      if (r.data.customerId) {
        getCustomerById(r.data.customerId).then((cr) => setCustomer(cr.data));
      }
    });
    getContracts('', 0, 50, id).then((r) => setContracts(r.data.data));
  }, [id]);

  if (!project) return <div className="text-gray-400">加载中...</div>;

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-gray-900">{project.name}</h1>

      <div className="bg-white rounded-xl border border-gray-200 p-5">
        <h2 className="text-sm font-medium text-gray-500 mb-3">基本信息</h2>
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div><span className="text-gray-400">项目编号</span><p className="font-medium">{project.projectNo || '-'}</p></div>
          <div><span className="text-gray-400">业务类型</span><p className="font-medium">{project.businessType || '-'}</p></div>
          <div><span className="text-gray-400">租赁类型</span><p className="font-medium">{project.leaseType || '-'}</p></div>
          <div>
            <span className="text-gray-400">所属客户</span>
            <p className="font-medium">
              {customer ? (
                <Link to={`/customers/${customer.id}`} className="text-blue-600 hover:underline">{customer.name}</Link>
              ) : '-'}
            </p>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-gray-200 p-5">
        <h2 className="text-sm font-medium text-gray-500 mb-3">关联合同 ({contracts.length})</h2>
        <div className="space-y-2">
          {contracts.map((c) => (
            <Link key={c.id} to={`/contracts/${c.id}`} className="flex items-center justify-between p-3 rounded-lg border border-gray-200 hover:bg-gray-50 transition-colors">
              <div>
                <p className="font-medium text-gray-900">{c.contractNo}</p>
                <p className="text-xs text-gray-500">{c.amount?.toLocaleString()} {c.currency} · {c.lessor} → {c.lessee}</p>
              </div>
              <div className="flex items-center gap-3">
                <span className="text-xs text-gray-400">{c.signDate ? dayjs(c.signDate).format('YYYY-MM-DD') : ''}</span>
                <StatusBadge value={c.status} />
              </div>
            </Link>
          ))}
          {contracts.length === 0 && <p className="text-sm text-gray-400">暂无关联合同</p>}
        </div>
      </div>
    </div>
  );
}
