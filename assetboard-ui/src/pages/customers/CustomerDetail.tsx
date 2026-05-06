import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getCustomerById } from '../../api/customer';
import { getProjectsByCustomerId } from '../../api/project';
import { getAssets } from '../../api/asset';
import { useAuth } from '../../hooks/useAuth';
import StatusBadge from '../../components/StatusBadge';
import type { Customer, Project, Asset } from '../../types';

export default function CustomerDetail() {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [projects, setProjects] = useState<Project[]>([]);
  const [assets, setAssets] = useState<Asset[]>([]);

  useEffect(() => {
    if (!id || !user) return;
    getCustomerById(id).then((r) => setCustomer(r.data));
    getProjectsByCustomerId(id, 0, 50).then((r) => setProjects(r.data.data));
    getAssets(user.tenantId, 0, 50, id).then((r) => setAssets(r.data.data));
  }, [id, user]);

  if (!customer) return <div className="text-gray-400">加载中...</div>;

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-gray-900">{customer.name}</h1>

      <div className="bg-white rounded-xl border border-gray-200 p-5">
        <h2 className="text-sm font-medium text-gray-500 mb-3">基本信息</h2>
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div><span className="text-gray-400">授信额度</span><p className="font-medium">{customer.creditAmount?.toLocaleString() ?? '-'}</p></div>
          <div><span className="text-gray-400">剩余本金</span><p className="font-medium">{customer.remainingPrincipal?.toLocaleString() ?? '-'}</p></div>
          <div className="col-span-2"><span className="text-gray-400">地址</span><p>{customer.address || '-'}</p></div>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-gray-200 p-5">
        <h2 className="text-sm font-medium text-gray-500 mb-3">关联项目 ({projects.length})</h2>
        <div className="space-y-2">
          {projects.map((p) => (
            <Link key={p.id} to={`/projects/${p.id}`} className="flex items-center justify-between p-3 rounded-lg border border-gray-200 hover:bg-gray-50 transition-colors">
              <div>
                <p className="font-medium text-gray-900">{p.name}</p>
                <p className="text-xs text-gray-500">{p.projectNo} · {p.businessType} · {p.leaseType}</p>
              </div>
              <span className="text-gray-400 text-sm">→</span>
            </Link>
          ))}
          {projects.length === 0 && <p className="text-sm text-gray-400">暂无关联项目</p>}
        </div>
      </div>

      <div className="bg-white rounded-xl border border-gray-200 p-5">
        <h2 className="text-sm font-medium text-gray-500 mb-3">关联资产 ({assets.length})</h2>
        <div className="space-y-2">
          {assets.map((a) => (
            <Link key={a.id} to={`/assets/${a.id}`} className="flex items-center justify-between p-3 rounded-lg border border-gray-200 hover:bg-gray-50 transition-colors">
              <div>
                <p className="font-medium text-gray-900">{a.name}</p>
                <p className="text-xs text-gray-500">{a.type}</p>
              </div>
              <StatusBadge value={a.status} />
            </Link>
          ))}
          {assets.length === 0 && <p className="text-sm text-gray-400">暂无关联资产</p>}
        </div>
      </div>
    </div>
  );
}
