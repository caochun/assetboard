import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getContractById } from '../../api/contract';
import { getProjectById } from '../../api/project';
import { getRelationsFrom } from '../../api/relation';
import { getAssetById } from '../../api/asset';
import StatusBadge from '../../components/StatusBadge';
import { ASSET_TYPE_LABELS } from '../../constants/dataSources';
import type { Contract, Project, Asset } from '../../types';
import dayjs from 'dayjs';

export default function ContractDetail() {
  const { id } = useParams<{ id: string }>();
  const [contract, setContract] = useState<Contract | null>(null);
  const [project, setProject] = useState<Project | null>(null);
  const [assets, setAssets] = useState<Asset[]>([]);

  useEffect(() => {
    if (!id) return;
    getContractById(id).then((r) => {
      setContract(r.data);
      if (r.data.projectId) {
        getProjectById(r.data.projectId).then((pr) => setProject(pr.data));
      }
    });
    getRelationsFrom(id, 'CONTRACT').then(async (r) => {
      const assetRelations = r.data.filter((rel) => rel.toType === 'ASSET');
      const assetPromises = assetRelations.map((rel) => getAssetById(rel.toId).then((ar) => ar.data));
      setAssets(await Promise.all(assetPromises));
    });
  }, [id]);

  if (!contract) return <div className="text-gray-400">加载中...</div>;

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-gray-900">合同 {contract.contractNo}</h1>

      <div className="bg-white rounded-xl border border-gray-200 p-5">
        <h2 className="text-sm font-medium text-gray-500 mb-3">基本信息</h2>
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div><span className="text-gray-400">合同编号</span><p className="font-medium">{contract.contractNo}</p></div>
          <div><span className="text-gray-400">金额</span><p className="font-medium">{contract.amount?.toLocaleString()} {contract.currency}</p></div>
          <div><span className="text-gray-400">出租方</span><p className="font-medium">{contract.lessor || '-'}</p></div>
          <div><span className="text-gray-400">承租方</span><p className="font-medium">{contract.lessee || '-'}</p></div>
          <div><span className="text-gray-400">状态</span><p><StatusBadge value={contract.status} /></p></div>
          <div><span className="text-gray-400">签约日期</span><p className="font-medium">{contract.signDate ? dayjs(contract.signDate).format('YYYY-MM-DD') : '-'}</p></div>
          <div>
            <span className="text-gray-400">所属项目</span>
            <p className="font-medium">
              {project ? (
                <Link to={`/projects/${project.id}`} className="text-blue-600 hover:underline">{project.name}</Link>
              ) : '-'}
            </p>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-gray-200 p-5">
        <h2 className="text-sm font-medium text-gray-500 mb-3">关联资产 ({assets.length})</h2>
        <div className="space-y-2">
          {assets.map((a) => (
            <Link key={a.id} to={`/assets/${a.id}`} className="flex items-center justify-between p-3 rounded-lg border border-gray-200 hover:bg-gray-50 transition-colors">
              <div>
                <p className="font-medium text-gray-900">{a.name}</p>
                <p className="text-xs text-gray-500">{ASSET_TYPE_LABELS[a.type] || a.type}</p>
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
