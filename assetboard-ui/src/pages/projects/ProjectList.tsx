import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getProjects, saveProject, deleteProject } from '../../api/project';
import { useAuth } from '../../hooks/useAuth';
import { usePagination } from '../../hooks/usePagination';
import PageHeader from '../../components/PageHeader';
import DataTable from '../../components/DataTable';
import type { Project, PageData } from '../../types';

export default function ProjectList() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { page, pageSize, goToPage } = usePagination();
  const [data, setData] = useState<PageData<Project>>({ data: [], totalPages: 0, totalElements: 0, hasNext: false });
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ name: '', projectNo: '', businessType: '', leaseType: '' });

  const load = () => {
    if (!user) return;
    getProjects(user.tenantId, page, pageSize).then((res) => setData(res.data));
  };

  useEffect(load, [user, page, pageSize]);

  const handleSave = async () => {
    if (!user) return;
    await saveProject({ tenantId: user.tenantId, ...form });
    setShowForm(false);
    setForm({ name: '', projectNo: '', businessType: '', leaseType: '' });
    load();
  };

  const handleDelete = async (id: string) => {
    await deleteProject(id);
    load();
  };

  const columns = [
    { key: 'name', title: '项目名称' },
    { key: 'projectNo', title: '项目编号' },
    { key: 'businessType', title: '业务类型' },
    { key: 'leaseType', title: '租赁类型' },
    { key: 'actions', title: '操作', render: (r: Project) => (
      <button onClick={(e) => { e.stopPropagation(); handleDelete(r.id); }} className="text-red-600 hover:underline text-xs">删除</button>
    )},
  ];

  return (
    <div>
      <PageHeader
        title="项目管理"
        action={<button onClick={() => setShowForm(true)} className="rounded-lg bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-700">新增项目</button>}
      />
      {showForm && (
        <div className="bg-white rounded-xl border border-gray-200 p-5 mb-5 space-y-3">
          <input placeholder="项目名称" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3.5 py-2.5 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent focus:bg-white transition-colors" />
          <input placeholder="项目编号" value={form.projectNo} onChange={(e) => setForm({ ...form, projectNo: e.target.value })} className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3.5 py-2.5 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent focus:bg-white transition-colors" />
          <input placeholder="业务类型" value={form.businessType} onChange={(e) => setForm({ ...form, businessType: e.target.value })} className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3.5 py-2.5 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent focus:bg-white transition-colors" />
          <input placeholder="租赁类型" value={form.leaseType} onChange={(e) => setForm({ ...form, leaseType: e.target.value })} className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3.5 py-2.5 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent focus:bg-white transition-colors" />
          <div className="flex gap-2">
            <button onClick={handleSave} className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700 transition-colors">保存</button>
            <button onClick={() => setShowForm(false)} className="rounded-lg border border-gray-200 px-4 py-2 text-sm font-medium text-gray-600 hover:bg-gray-50 transition-colors">取消</button>
          </div>
        </div>
      )}
      <DataTable columns={columns} data={data.data} page={page} totalPages={data.totalPages} onPageChange={goToPage} onRowClick={(r) => navigate(`/projects/${r.id}`)} />
    </div>
  );
}
