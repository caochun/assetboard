import { useEffect, useState } from 'react';
import { getAlarms, acknowledgeAlarm, clearAlarm } from '../../api/alarm';
import { useAuth } from '../../hooks/useAuth';
import { usePagination } from '../../hooks/usePagination';
import PageHeader from '../../components/PageHeader';
import StatusBadge from '../../components/StatusBadge';
import { translateAlarmType, SEVERITY_LABELS } from '../../constants/dataSources';
import type { Alarm, PageData } from '../../types';
import dayjs from 'dayjs';

export default function AlarmList() {
  const { user } = useAuth();
  const { page, pageSize, goToPage } = usePagination();
  const [data, setData] = useState<PageData<Alarm>>({ data: [], totalPages: 0, totalElements: 0, hasNext: false });
  const [filter, setFilter] = useState<string>('ALL');

  const load = () => {
    if (!user) return;
    getAlarms(user.tenantId, page, pageSize).then((res) => setData(res.data));
  };

  useEffect(load, [user, page, pageSize]);

  const handleAck = async (id: string) => {
    await acknowledgeAlarm(id);
    load();
  };

  const handleClear = async (id: string) => {
    await clearAlarm(id);
    load();
  };

  const filtered = filter === 'ALL'
    ? data.data
    : filter === 'UNACKED'
      ? data.data.filter((a) => !a.acknowledged && !a.cleared)
      : data.data.filter((a) => a.severity === filter);

  return (
    <div>
      <PageHeader title="告警中心" />
      <div className="flex gap-2 mb-4">
        {['ALL', 'UNACKED', 'CRITICAL', 'MAJOR', 'MINOR', 'WARNING'].map((f) => (
          <button
            key={f}
            onClick={() => setFilter(f)}
            className={`rounded-full px-3 py-1 text-xs font-medium transition-colors ${
              filter === f ? 'bg-blue-100 text-blue-700' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            {f === 'ALL' ? '全部' : f === 'UNACKED' ? '待处理' : SEVERITY_LABELS[f] || f}
          </button>
        ))}
      </div>
      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50/60">
            <tr className="text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">
              <th className="px-5 py-3">类型</th>
              <th className="px-5 py-3">级别</th>
              <th className="px-5 py-3">时间</th>
              <th className="px-5 py-3">状态</th>
              <th className="px-5 py-3">操作</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map((alarm) => (
              <tr key={alarm.id} className="border-t border-gray-100 hover:bg-blue-50/40 transition-colors">
                <td className="px-5 py-3.5 font-medium text-gray-900">{translateAlarmType(alarm.type)}</td>
                <td className="px-5 py-3.5"><StatusBadge value={alarm.severity} /></td>
                <td className="px-5 py-3.5 text-gray-500">{dayjs(alarm.createdTime).format('YYYY-MM-DD HH:mm')}</td>
                <td className="px-5 py-3.5 text-gray-600">{alarm.cleared ? '已清除' : alarm.acknowledged ? '已确认' : '待处理'}</td>
                <td className="px-5 py-3.5 space-x-2">
                  {!alarm.acknowledged && !alarm.cleared && (
                    <button onClick={() => handleAck(alarm.id)} className="text-blue-600 hover:underline text-xs">确认</button>
                  )}
                  {!alarm.cleared && (
                    <button onClick={() => handleClear(alarm.id)} className="text-green-600 hover:underline text-xs">清除</button>
                  )}
                </td>
              </tr>
            ))}
            {filtered.length === 0 && (
              <tr><td colSpan={5} className="px-5 py-12 text-center text-sm text-gray-400">暂无告警</td></tr>
            )}
          </tbody>
        </table>
      </div>
      {data.totalPages > 1 && (
        <div className="flex items-center justify-between border-t border-gray-100 bg-white rounded-b-xl px-5 py-3 mt-0">
          <button onClick={() => goToPage(page - 1)} disabled={page === 0} className="rounded-lg border border-gray-200 px-3.5 py-1.5 text-sm font-medium text-gray-600 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors">上一页</button>
          <span className="text-sm text-gray-500">{page + 1} / {data.totalPages}</span>
          <button onClick={() => goToPage(page + 1)} disabled={!data.hasNext} className="rounded-lg border border-gray-200 px-3.5 py-1.5 text-sm font-medium text-gray-600 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors">下一页</button>
        </div>
      )}
    </div>
  );
}
