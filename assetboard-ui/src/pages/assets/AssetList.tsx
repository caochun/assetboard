import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAssets } from '../../api/asset';
import { useAuth } from '../../hooks/useAuth';
import { usePagination } from '../../hooks/usePagination';
import PageHeader from '../../components/PageHeader';
import DataTable from '../../components/DataTable';
import StatusBadge from '../../components/StatusBadge';
import { ASSET_TYPE_LABELS } from '../../constants/dataSources';
import type { Asset, PageData } from '../../types';

export default function AssetList() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { page, pageSize, goToPage } = usePagination();
  const [data, setData] = useState<PageData<Asset>>({ data: [], totalPages: 0, totalElements: 0, hasNext: false });

  useEffect(() => {
    if (!user) return;
    getAssets(user.tenantId, page, pageSize).then((res) => setData(res.data));
  }, [user, page, pageSize]);

  const columns = [
    { key: 'name', title: '名称' },
    { key: 'type', title: '类型', render: (row: Asset) => ASSET_TYPE_LABELS[row.type] || row.type },
    { key: 'status', title: '状态', render: (row: Asset) => <StatusBadge value={row.status} /> },
    { key: 'label', title: '标签' },
  ];

  return (
    <div>
      <PageHeader title="资产管理" />
      <DataTable
        columns={columns}
        data={data.data}
        page={page}
        totalPages={data.totalPages}
        onPageChange={goToPage}
        onRowClick={(row) => navigate(`/assets/${row.id}`)}
      />
    </div>
  );
}
