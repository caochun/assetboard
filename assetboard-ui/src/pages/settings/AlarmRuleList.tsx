import { useEffect, useState } from 'react';
import { getAlarmRules, saveAlarmRule, deleteAlarmRule } from '../../api/alarmRule';
import { useAuth } from '../../hooks/useAuth';
import { usePagination } from '../../hooks/usePagination';
import PageHeader from '../../components/PageHeader';
import DataTable from '../../components/DataTable';
import StatusBadge from '../../components/StatusBadge';
import type { AlarmRule, PageData } from '../../types';

export default function AlarmRuleList() {
  const { user } = useAuth();
  const { page, pageSize, goToPage } = usePagination();
  const [data, setData] = useState<PageData<AlarmRule>>({ data: [], totalPages: 0, totalElements: 0, hasNext: false });
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({
    name: '', targetType: 'vessel', telemetryKey: 'roughValue',
    condition: 'LT', threshold: '45', severity: 'MAJOR', alarmType: 'VALUATION_DROP', enabled: true,
  });

  const load = () => {
    if (!user) return;
    getAlarmRules(user.tenantId, page, pageSize).then((res) => setData(res.data));
  };

  useEffect(load, [user, page, pageSize]);

  const handleSave = async () => {
    if (!user) return;
    await saveAlarmRule({
      tenantId: user.tenantId,
      name: form.name,
      targetType: form.targetType,
      telemetryKey: form.telemetryKey,
      condition: form.condition,
      threshold: Number(form.threshold),
      severity: form.severity,
      alarmType: form.alarmType,
      enabled: form.enabled,
    });
    setShowForm(false);
    setForm({ name: '', targetType: 'vessel', telemetryKey: 'roughValue', condition: 'LT', threshold: '45', severity: 'MAJOR', alarmType: 'VALUATION_DROP', enabled: true });
    load();
  };

  const handleDelete = async (id: string) => {
    await deleteAlarmRule(id);
    load();
  };

  const columns = [
    { key: 'name', title: '规则名称' },
    { key: 'targetType', title: '资产类型', render: (r: AlarmRule) => {
      const map: Record<string, string> = { vessel: '船舶', aircraft: '飞机', equipment: '工程机械' };
      return r.targetType ? (map[r.targetType] || r.targetType) : '-';
    }},
    { key: 'telemetryKey', title: '监控指标' },
    { key: 'condition', title: '条件', render: (r: AlarmRule) => `${r.condition} ${r.threshold}` },
    { key: 'severity', title: '告警级别', render: (r: AlarmRule) => <StatusBadge value={r.severity} /> },
    { key: 'alarmType', title: '告警类型' },
    { key: 'enabled', title: '状态', render: (r: AlarmRule) => (
      <span className={`text-xs font-medium ${r.enabled ? 'text-green-600' : 'text-gray-400'}`}>{r.enabled ? '启用' : '禁用'}</span>
    )},
    { key: 'actions', title: '操作', render: (r: AlarmRule) => (
      <button onClick={(e) => { e.stopPropagation(); handleDelete(r.id); }} className="text-red-600 hover:underline text-xs">删除</button>
    )},
  ];

  return (
    <div>
      <PageHeader
        title="告警规则管理"
        action={<button onClick={() => setShowForm(true)} className="rounded-lg bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-700">新增规则</button>}
      />
      {showForm && (
        <div className="bg-white rounded-xl border border-gray-200 p-5 mb-5 space-y-3">
          <input placeholder="规则名称" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3.5 py-2.5 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent focus:bg-white transition-colors" />
          <select value={form.targetType} onChange={(e) => setForm({ ...form, targetType: e.target.value })} className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3.5 py-2.5 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent focus:bg-white transition-colors">
            <option value="vessel">船舶</option>
            <option value="aircraft">飞机</option>
            <option value="equipment">工程机械</option>
          </select>
          <div className="grid grid-cols-2 gap-3">
            <input placeholder="监控指标 (key)" value={form.telemetryKey} onChange={(e) => setForm({ ...form, telemetryKey: e.target.value })} className="rounded-lg border border-gray-200 bg-gray-50 px-3.5 py-2.5 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent focus:bg-white transition-colors" />
            <select value={form.condition} onChange={(e) => setForm({ ...form, condition: e.target.value })} className="rounded-lg border border-gray-200 bg-gray-50 px-3.5 py-2.5 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent focus:bg-white transition-colors">
              <option value="GT">大于 (GT)</option>
              <option value="GTE">大于等于 (GTE)</option>
              <option value="LT">小于 (LT)</option>
              <option value="LTE">小于等于 (LTE)</option>
              <option value="EQ">等于 (EQ)</option>
            </select>
            <input placeholder="阈值" type="number" value={form.threshold} onChange={(e) => setForm({ ...form, threshold: e.target.value })} className="rounded-lg border border-gray-200 bg-gray-50 px-3.5 py-2.5 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent focus:bg-white transition-colors" />
            <select value={form.severity} onChange={(e) => setForm({ ...form, severity: e.target.value })} className="rounded-lg border border-gray-200 bg-gray-50 px-3.5 py-2.5 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent focus:bg-white transition-colors">
              <option value="CRITICAL">CRITICAL</option>
              <option value="MAJOR">MAJOR</option>
              <option value="MINOR">MINOR</option>
              <option value="WARNING">WARNING</option>
            </select>
          </div>
          <input placeholder="告警类型" value={form.alarmType} onChange={(e) => setForm({ ...form, alarmType: e.target.value })} className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3.5 py-2.5 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent focus:bg-white transition-colors" />
          <label className="flex items-center gap-2 text-sm text-gray-700">
            <input type="checkbox" checked={form.enabled} onChange={(e) => setForm({ ...form, enabled: e.target.checked })} className="rounded border-gray-300 text-blue-600 focus:ring-blue-500" />
            启用
          </label>
          <div className="flex gap-2">
            <button onClick={handleSave} className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700 transition-colors">保存</button>
            <button onClick={() => setShowForm(false)} className="rounded-lg border border-gray-200 px-4 py-2 text-sm font-medium text-gray-600 hover:bg-gray-50 transition-colors">取消</button>
          </div>
        </div>
      )}
      <DataTable columns={columns} data={data.data} page={page} totalPages={data.totalPages} onPageChange={goToPage} />
    </div>
  );
}
