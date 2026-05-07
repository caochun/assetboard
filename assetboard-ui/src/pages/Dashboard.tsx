import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ReactECharts from 'echarts-for-react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { getAssets, getLatestTimeseries } from '../api/asset';
import { getAlarms } from '../api/alarm';
import { getContracts } from '../api/contract';
import { useAuth } from '../hooks/useAuth';
import StatusBadge from '../components/StatusBadge';
import { translateAlarmType } from '../constants/dataSources';
import type { Asset, Alarm, Contract } from '../types';
import dayjs from 'dayjs';

const defaultIcon = L.icon({
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
});

export default function Dashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [assets, setAssets] = useState<Asset[]>([]);
  const [allAlarms, setAllAlarms] = useState<Alarm[]>([]);
  const [recentAlarms, setRecentAlarms] = useState<Alarm[]>([]);
  const [contracts, setContracts] = useState<Contract[]>([]);
  const [shipPositions, setShipPositions] = useState<{ asset: Asset; lat: number; lon: number }[]>([]);

  useEffect(() => {
    if (!user) return;
    const tid = user.tenantId;

    getAssets(tid, 0, 100).then((res) => {
      const list = res.data.data;
      setAssets(list);
      list.filter((a) => a.type === 'vessel').forEach((asset) => {
        getLatestTimeseries(asset.id).then((tsRes) => {
          const lat = tsRes.data.find((e) => e.key === 'lat');
          const lon = tsRes.data.find((e) => e.key === 'lon');
          if (lat && lon) {
            setShipPositions((prev) => [
              ...prev.filter((p) => p.asset.id !== asset.id),
              { asset, lat: Number(lat.value), lon: Number(lon.value) },
            ]);
          }
        });
      });
    });

    getAlarms(tid, 0, 100).then((res) => {
      setAllAlarms(res.data.data);
      setRecentAlarms(res.data.data.slice(0, 8));
    });

    getContracts('', 0, 100).then((res) => {
      setContracts(res.data.data);
    });
  }, [user]);

  // --- Derived stats ---
  const vesselCount = assets.filter((a) => a.type === 'vessel').length;
  const aircraftCount = assets.filter((a) => a.type === 'aircraft').length;
  const equipmentCount = assets.filter((a) => a.type === 'equipment').length;
  const activeAlarmCount = allAlarms.filter((a) => !a.cleared).length;

  // --- Stat cards ---
  const statItems = [
    { label: '资产总数', value: assets.length, icon: '📦', bg: 'bg-blue-50', text: 'text-blue-700' },
    { label: '船舶', value: vesselCount, icon: '🚢', bg: 'bg-blue-50', text: 'text-blue-700' },
    { label: '飞机', value: aircraftCount, icon: '✈️', bg: 'bg-purple-50', text: 'text-purple-700' },
    { label: '工程机械', value: equipmentCount, icon: '🔧', bg: 'bg-amber-50', text: 'text-amber-700' },
    { label: '活跃告警', value: activeAlarmCount, icon: '🔔', bg: 'bg-red-50', text: 'text-red-700' },
  ];

  // --- Asset status donut ---
  const statusMap: Record<string, string> = { IN_LEASE: '在租', DISPOSED: '已处置', REPURCHASED: '已回购' };
  const statusColors: Record<string, string> = { IN_LEASE: '#22c55e', DISPOSED: '#9ca3af', REPURCHASED: '#3b82f6' };
  const statusGroups = assets.reduce<Record<string, number>>((acc, a) => {
    acc[a.status] = (acc[a.status] || 0) + 1;
    return acc;
  }, {});
  const statusPieOption = {
    tooltip: { trigger: 'item' as const },
    legend: { bottom: 0, textStyle: { color: '#6b7280', fontSize: 12 } },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      label: { show: true, formatter: '{b}: {c}', fontSize: 12, color: '#374151' },
      data: Object.entries(statusGroups).map(([k, v]) => ({
        name: statusMap[k] || k,
        value: v,
        itemStyle: { color: statusColors[k] || '#6b7280' },
      })),
    }],
  };

  // --- Alarm severity donut ---
  const severityColors: Record<string, string> = { CRITICAL: '#ef4444', MAJOR: '#f97316', MINOR: '#eab308', WARNING: '#f59e0b', INDETERMINATE: '#9ca3af' };
  const unclearedAlarms = allAlarms.filter((a) => !a.cleared);
  const severityGroups = unclearedAlarms.reduce<Record<string, number>>((acc, a) => {
    acc[a.severity] = (acc[a.severity] || 0) + 1;
    return acc;
  }, {});
  const severityPieOption = {
    tooltip: { trigger: 'item' as const },
    legend: { bottom: 0, textStyle: { color: '#6b7280', fontSize: 12 } },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      label: { show: true, formatter: '{b}: {c}', fontSize: 12, color: '#374151' },
      data: Object.entries(severityGroups).map(([k, v]) => ({
        name: k,
        value: v,
        itemStyle: { color: severityColors[k] || '#6b7280' },
      })),
    }],
  };

  // --- Contract amount bar chart ---
  const activeContracts = contracts.filter((c) => c.status === 'ACTIVE');
  const contractByLessee = activeContracts.reduce<Record<string, number>>((acc, c) => {
    const name = (c.lessee || '未知').replace(/股份有限公司|有限公司/g, '');
    const amountInYi = (c.amount || 0) / (c.currency === 'CNY' ? 100000000 : 100000000);
    acc[name] = (acc[name] || 0) + amountInYi;
    return acc;
  }, {});
  const contractBarNames = Object.keys(contractByLessee);
  const contractBarValues = Object.values(contractByLessee).map((v) => Math.round(v * 100) / 100);
  const contractBarOption = {
    tooltip: { trigger: 'axis' as const, formatter: (params: { name: string; value: number }[]) => `${params[0].name}<br/>合同金额: ${params[0].value} 亿` },
    xAxis: {
      type: 'category' as const,
      data: contractBarNames,
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisLabel: { color: '#6b7280', fontSize: 11, rotate: contractBarNames.length > 4 ? 15 : 0 },
    },
    yAxis: {
      type: 'value' as const,
      name: '亿元',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f3f4f6' } },
      axisLabel: { color: '#9ca3af' },
    },
    series: [{
      type: 'bar',
      data: contractBarValues,
      barWidth: '40%',
      itemStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [{ offset: 0, color: '#3b82f6' }, { offset: 1, color: '#93c5fd' }],
        },
        borderRadius: [4, 4, 0, 0],
      },
    }],
    grid: { left: 50, right: 20, top: 35, bottom: 40 },
  };

  // --- Map center ---
  const center: [number, number] = shipPositions.length > 0
    ? [shipPositions[0].lat, shipPositions[0].lon]
    : [30, 120];

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-gray-900">总览</h1>

      {/* Stat Cards */}
      <div className="grid grid-cols-5 gap-4">
        {statItems.map((item) => (
          <div key={item.label} className="bg-white rounded-xl border border-gray-200 p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs text-gray-500 mb-1">{item.label}</p>
                <p className={`text-2xl font-bold ${item.text}`}>{item.value}</p>
              </div>
              <div className={`w-10 h-10 rounded-xl ${item.bg} flex items-center justify-center text-lg`}>
                {item.icon}
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Donut Charts Row */}
      <div className="grid grid-cols-2 gap-5">
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          <h2 className="text-sm font-semibold text-gray-900 mb-2">资产状态分布</h2>
          {assets.length > 0 ? (
            <ReactECharts option={statusPieOption} style={{ height: 260 }} />
          ) : (
            <div className="flex items-center justify-center h-[260px] text-gray-400 text-sm">加载中...</div>
          )}
        </div>
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          <h2 className="text-sm font-semibold text-gray-900 mb-2">告警级别分布</h2>
          {unclearedAlarms.length > 0 ? (
            <ReactECharts option={severityPieOption} style={{ height: 260 }} />
          ) : (
            <div className="flex items-center justify-center h-[260px] text-gray-400 text-sm">暂无活跃告警</div>
          )}
        </div>
      </div>

      {/* Bar Chart + Map Row */}
      <div className="grid grid-cols-2 gap-5">
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          <h2 className="text-sm font-semibold text-gray-900 mb-2">合同金额概览（ACTIVE）</h2>
          {contractBarNames.length > 0 ? (
            <ReactECharts option={contractBarOption} style={{ height: 280 }} />
          ) : (
            <div className="flex items-center justify-center h-[280px] text-gray-400 text-sm">加载中...</div>
          )}
        </div>
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          <h2 className="text-sm font-semibold text-gray-900 mb-2">船舶位置分布</h2>
          <MapContainer center={center} zoom={3} style={{ height: 280, borderRadius: 12 }}>
            <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
            {shipPositions.map((sp) => (
              <Marker key={sp.asset.id} position={[sp.lat, sp.lon]} icon={defaultIcon}>
                <Popup>{sp.asset.name}</Popup>
              </Marker>
            ))}
          </MapContainer>
        </div>
      </div>

      {/* Recent Alarms Table */}
      <div className="bg-white rounded-xl border border-gray-200 p-5">
        <h2 className="text-sm font-semibold text-gray-900 mb-4">最近告警</h2>
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left text-xs font-semibold text-gray-500 uppercase tracking-wider border-b border-gray-100">
              <th className="pb-3">类型</th>
              <th className="pb-3">严重级别</th>
              <th className="pb-3">时间</th>
              <th className="pb-3">状态</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-50">
            {recentAlarms.map((alarm) => (
              <tr key={alarm.id} className="cursor-pointer hover:bg-blue-50/40 transition-colors" onClick={() => navigate('/alarms')}>
                <td className="py-3 font-medium text-gray-900">{translateAlarmType(alarm.type)}</td>
                <td className="py-3"><StatusBadge value={alarm.severity} /></td>
                <td className="py-3 text-gray-500">{dayjs(alarm.createdTime).format('YYYY-MM-DD HH:mm')}</td>
                <td className="py-3 text-gray-600">{alarm.cleared ? '已清除' : alarm.acknowledged ? '已确认' : '待处理'}</td>
              </tr>
            ))}
            {recentAlarms.length === 0 && (
              <tr><td colSpan={4} className="py-8 text-center text-gray-400">暂无告警</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
