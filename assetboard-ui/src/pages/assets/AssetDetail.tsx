import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import ReactECharts from 'echarts-for-react';
import { MapContainer, TileLayer, Polyline, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { getAssetById, getAttributes, getTimeseries, getLatestTimeseries } from '../../api/asset';
import { getAlarmsByOriginator } from '../../api/alarm';
import { getRelationsTo } from '../../api/relation';
import { getContractById } from '../../api/contract';
import { getDataSourceConfigs, saveDataSourceConfig, deleteDataSourceConfig } from '../../api/dataSourceConfig';
import StatusBadge from '../../components/StatusBadge';
import { DATA_SOURCES, getDataSourceById, getTimeseriesSource, getAlarmSource, translateAlarmType, translateTsKey, ASSET_TYPE_LABELS } from '../../constants/dataSources';
import type { Asset, AttributeKvEntry, TsKvEntry, Alarm, Contract, DataSourceConfig } from '../../types';
import dayjs from 'dayjs';

const defaultIcon = L.icon({
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
});

type Tab = 'info' | 'timeseries' | 'trajectory' | 'alarms' | 'contracts' | 'datasources';

export default function AssetDetail() {
  const { id } = useParams<{ id: string }>();
  const [asset, setAsset] = useState<Asset | null>(null);
  const [attributes, setAttributes] = useState<AttributeKvEntry[]>([]);
  const [latestTs, setLatestTs] = useState<TsKvEntry[]>([]);
  const [alarms, setAlarms] = useState<Alarm[]>([]);
  const [contracts, setContracts] = useState<Contract[]>([]);
  const [dsConfigs, setDsConfigs] = useState<DataSourceConfig[]>([]);
  const [showAddDs, setShowAddDs] = useState(false);
  const [tab, setTab] = useState<Tab>('info');
  const [tsKey, setTsKey] = useState('roughValue');
  const [tsData, setTsData] = useState<TsKvEntry[]>([]);
  const [trackData, setTrackData] = useState<[number, number][]>([]);

  useEffect(() => {
    if (!id) return;
    getAssetById(id).then((r) => setAsset(r.data));
    getAttributes(id).then((r) => setAttributes(r.data));
    getLatestTimeseries(id).then((r) => setLatestTs(r.data));
    getAlarmsByOriginator(id, 0, 50).then((r) => setAlarms(r.data.data));
    getRelationsTo(id, 'ASSET').then(async (r) => {
      const contractRels = r.data.filter((rel) => rel.fromType === 'CONTRACT');
      const results = await Promise.all(contractRels.map((rel) => getContractById(rel.fromId).then((cr) => cr.data)));
      setContracts(results);
    });
    getDataSourceConfigs(id).then((r) => setDsConfigs(r.data));
  }, [id]);

  useEffect(() => {
    if (!id || tab !== 'timeseries') return;
    const now = Date.now();
    getTimeseries(id, tsKey, now - 365 * 86400000, now, 500).then((r) => setTsData(r.data.sort((a, b) => a.ts - b.ts)));
  }, [id, tab, tsKey]);

  useEffect(() => {
    if (!id || tab !== 'trajectory') return;
    const now = Date.now();
    Promise.all([
      getTimeseries(id, 'lat', now - 30 * 86400000, now, 500),
      getTimeseries(id, 'lon', now - 30 * 86400000, now, 500),
    ]).then(([latRes, lonRes]) => {
      const latMap = new Map(latRes.data.map((e) => [e.ts, Number(e.value)]));
      const positions: [number, number][] = [];
      lonRes.data.sort((a, b) => a.ts - b.ts).forEach((e) => {
        const lat = latMap.get(e.ts);
        if (lat) positions.push([lat, Number(e.value)]);
      });
      setTrackData(positions);
    });
  }, [id, tab]);

  if (!asset) return <div className="text-gray-400">加载中...</div>;

  function resolveApiParams(template: string, a: Asset, ts: TsKvEntry[]): string {
    let result = template;
    const imo = a.additionalInfo?.imo;
    if (imo) result = result.replace(/\{imo\}/g, String(imo));
    const latEntry = ts.find((e) => e.key === 'lat');
    const lonEntry = ts.find((e) => e.key === 'lon');
    if (latEntry) result = result.replace(/\{lat\}/g, String(latEntry.value));
    if (lonEntry) result = result.replace(/\{lon\}/g, String(lonEntry.value));
    return result;
  }

  const tsKeys = [...new Set(latestTs.map((e) => e.key))];

  const chartOption = {
    tooltip: { trigger: 'axis' as const },
    xAxis: { type: 'category' as const, data: tsData.map((e) => dayjs(e.ts).format('MM-DD HH:mm')), axisLine: { lineStyle: { color: '#e5e7eb' } }, axisLabel: { color: '#9ca3af' } },
    yAxis: { type: 'value' as const, axisLine: { show: false }, splitLine: { lineStyle: { color: '#f3f4f6' } }, axisLabel: { color: '#9ca3af' } },
    series: [{ data: tsData.map((e) => Number(e.value)), type: 'line', smooth: true, lineStyle: { color: '#3b82f6', width: 2 }, areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(59,130,246,0.12)' }, { offset: 1, color: 'rgba(59,130,246,0)' }] } }, itemStyle: { color: '#3b82f6' } }],
    grid: { left: 60, right: 20, top: 20, bottom: 30 },
  };

  const center: [number, number] = trackData.length > 0
    ? trackData[trackData.length - 1]
    : [30, 120];

  const tabs: { key: Tab; label: string }[] = [
    { key: 'info', label: '基本信息' },
    { key: 'timeseries', label: '时序数据' },
    { key: 'trajectory', label: '轨迹回放' },
    { key: 'alarms', label: '关联告警' },
    { key: 'contracts', label: '关联合同' },
    { key: 'datasources', label: '数据源' },
  ];

  return (
    <div>
      <h1 className="text-xl font-semibold text-gray-900 mb-4">{asset.name}</h1>

      <div className="flex gap-1 border-b border-gray-200 mb-4">
        {tabs.map((t) => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${
              tab === t.key ? 'border-blue-500 text-blue-600' : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'info' && (
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          <div className="grid grid-cols-2 gap-4 mb-6">
            <div><span className="text-xs text-gray-500">类型</span><p className="text-sm font-medium text-gray-900">{ASSET_TYPE_LABELS[asset.type] || asset.type}</p></div>
            <div><span className="text-xs text-gray-500">状态</span><p><StatusBadge value={asset.status} /></p></div>
            <div><span className="text-xs text-gray-500">标签</span><p className="text-sm font-medium text-gray-900">{asset.label}</p></div>
          </div>
          <h3 className="text-sm font-medium text-gray-700 mb-2">属性列表</h3>
          <div className="overflow-hidden rounded border border-gray-200">
            <table className="w-full text-sm">
              <thead className="bg-gray-50">
                <tr><th className="px-3 py-2 text-left text-xs text-gray-500">Key</th><th className="px-3 py-2 text-left text-xs text-gray-500">Value</th><th className="px-3 py-2 text-left text-xs text-gray-500">数据来源</th></tr>
              </thead>
              <tbody>
                {attributes.map((attr) => (
                  <tr key={attr.key} className="border-t">
                    <td className="px-3 py-2 font-mono text-xs">{attr.key}</td>
                    <td className="px-3 py-2 text-xs">{String(attr.value)}</td>
                    <td className="px-3 py-2 text-xs text-gray-400">{asset.type === 'vessel' ? '船舶档案(ShipXy)' : '系统录入'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {tab === 'timeseries' && (
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          <div className="flex gap-2 mb-4">
            <select
              value={tsKey}
              onChange={(e) => setTsKey(e.target.value)}
              className="rounded-lg border border-gray-200 bg-gray-50 px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent focus:bg-white transition-colors"
            >
              {tsKeys.map((k) => <option key={k} value={k}>{translateTsKey(k)}</option>)}
            </select>
            {getTimeseriesSource(tsKey) && (
              <span className="text-xs text-gray-400 self-center">数据来源: {getTimeseriesSource(tsKey)}</span>
            )}
          </div>
          <ReactECharts option={chartOption} style={{ height: 350 }} />
        </div>
      )}

      {tab === 'trajectory' && (
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          {trackData.length > 0 ? (
            <MapContainer center={center} zoom={6} style={{ height: 450, borderRadius: 12 }}>
              <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
              <Polyline positions={trackData} color="#3b82f6" weight={3} />
              <Marker position={trackData[trackData.length - 1]} icon={defaultIcon}>
                <Popup>{asset.name} - 最新位置</Popup>
              </Marker>
            </MapContainer>
          ) : (
            <div className="flex items-center justify-center h-[450px] text-gray-400 text-sm">暂无轨迹数据</div>
          )}
        </div>
      )}

      {tab === 'alarms' && (
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-xs font-semibold text-gray-500 uppercase tracking-wider border-b border-gray-100">
                <th className="pb-3">类型</th><th className="pb-3">级别</th><th className="pb-3">时间</th><th className="pb-3">状态</th><th className="pb-3">数据来源</th>
              </tr>
            </thead>
            <tbody>
              {alarms.map((a) => (
                <tr key={a.id} className="border-b">
                  <td className="py-2">{translateAlarmType(a.type)}</td>
                  <td className="py-2"><StatusBadge value={a.severity} /></td>
                  <td className="py-2 text-gray-500">{dayjs(a.createdTime).format('YYYY-MM-DD HH:mm')}</td>
                  <td className="py-2">{a.cleared ? '已清除' : a.acknowledged ? '已确认' : '待处理'}</td>
                  <td className="py-2 text-gray-400 text-xs">{getAlarmSource(a.type)}</td>
                </tr>
              ))}
              {alarms.length === 0 && <tr><td colSpan={5} className="py-4 text-center text-gray-400">暂无告警</td></tr>}
            </tbody>
          </table>
        </div>
      )}
      {tab === 'datasources' && (() => {
        const boundIds = new Set(dsConfigs.map((c) => c.collectorId));
        const availableToAdd = DATA_SOURCES.filter((d) => !boundIds.has(d.id));

        const handleAdd = async (collectorId: string) => {
          const result = await saveDataSourceConfig({ assetId: id, collectorId, enabled: true });
          setDsConfigs((prev) => [...prev, result.data]);
          setShowAddDs(false);
        };

        const handleRemove = async (config: DataSourceConfig) => {
          await deleteDataSourceConfig(config.id);
          setDsConfigs((prev) => prev.filter((c) => c.id !== config.id));
        };

        const handleToggle = async (config: DataSourceConfig) => {
          const result = await saveDataSourceConfig({ ...config, enabled: !config.enabled });
          setDsConfigs((prev) => prev.map((c) => c.id === config.id ? result.data : c));
        };

        return (
          <div>
            <div className="flex items-center justify-between mb-4">
              <span className="text-sm text-gray-500">已绑定 {dsConfigs.length} 个数据源</span>
              <button
                onClick={() => setShowAddDs(true)}
                className="rounded-lg bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-700 transition-colors"
              >
                添加数据源
              </button>
            </div>

            {showAddDs && (
              <div className="bg-white rounded-xl border border-gray-200 p-5 mb-4">
                <div className="flex items-center justify-between mb-3">
                  <h3 className="text-sm font-semibold text-gray-900">选择要添加的数据源</h3>
                  <button onClick={() => setShowAddDs(false)} className="text-xs text-gray-400 hover:text-gray-600">关闭</button>
                </div>
                {availableToAdd.length === 0 ? (
                  <p className="text-sm text-gray-400">所有数据源已绑定</p>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                    {availableToAdd.map((ds) => {
                      const isRecommended = (ds.assetType || 'vessel') === asset.type;
                      return (
                        <button
                          key={ds.id}
                          onClick={() => handleAdd(ds.id)}
                          className="text-left p-3 rounded-lg border border-gray-200 hover:border-blue-300 hover:bg-blue-50/50 transition-colors"
                        >
                          <div className="flex items-center gap-2 mb-1">
                            <span className="text-sm font-medium text-gray-900">{ds.name}</span>
                            {isRecommended && (
                              <span className="inline-flex items-center rounded-full bg-blue-100 px-1.5 py-0.5 text-xs font-medium text-blue-700">推荐</span>
                            )}
                          </div>
                          <p className="text-xs text-gray-500">{ds.description}</p>
                          <p className="text-xs text-gray-400 mt-1">{ds.provider} · {ds.interval} · {ASSET_TYPE_LABELS[ds.assetType || 'vessel']}</p>
                        </button>
                      );
                    })}
                  </div>
                )}
              </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {dsConfigs.map((config) => {
                const ds = getDataSourceById(config.collectorId);
                if (!ds) return (
                  <div key={config.id} className="bg-white rounded-xl border border-gray-200 p-5">
                    <p className="text-sm text-gray-400">未知数据源: {config.collectorId}</p>
                    <button onClick={() => handleRemove(config)} className="text-xs text-red-500 hover:underline mt-2">移除</button>
                  </div>
                );

                let lastTime: number | undefined;
                let hasData = false;
                let detail = '';

                if (ds.type === 'timeseries' && ds.tsKeys) {
                  const matching = latestTs.filter((e) => ds.tsKeys!.includes(e.key));
                  if (matching.length > 0) {
                    lastTime = Math.max(...matching.map((e) => e.ts));
                    hasData = true;
                    detail = `${matching.length} 个指标`;
                  }
                } else if (ds.type === 'attribute') {
                  if (attributes.length > 0) {
                    lastTime = Math.max(...attributes.map((a) => a.lastUpdateTs));
                    hasData = true;
                    detail = `${attributes.length} 个属性`;
                  }
                } else if (ds.type === 'alarm' && ds.alarmTypePatterns) {
                  const matching = alarms.filter((a) =>
                    ds.alarmTypePatterns!.some((p) => a.type === p || a.type.startsWith(p))
                  );
                  if (matching.length > 0) {
                    lastTime = Math.max(...matching.map((a) => a.createdTime));
                    hasData = true;
                    detail = `${matching.length} 条告警`;
                  }
                }

                const statusLabel = !config.enabled ? '已停用' : hasData ? '采集中' : '无数据';
                const statusClass = !config.enabled
                  ? 'bg-red-100 text-red-700'
                  : hasData ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-500';

                return (
                  <div key={config.id} className={`bg-white rounded-xl border border-gray-200 p-5 transition-opacity ${!config.enabled ? 'opacity-50' : ''}`}>
                    <div className="flex items-center justify-between mb-2">
                      <h3 className="text-sm font-medium text-gray-900">{ds.name}</h3>
                      <div className="flex items-center gap-2">
                        <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${statusClass}`}>
                          {statusLabel}
                        </span>
                        <button
                          onClick={() => handleToggle(config)}
                          className={`relative inline-flex h-5 w-9 items-center rounded-full transition-colors ${config.enabled ? 'bg-blue-600' : 'bg-gray-300'}`}
                        >
                          <span className={`inline-block h-3.5 w-3.5 rounded-full bg-white transition-transform ${config.enabled ? 'translate-x-4.5' : 'translate-x-0.5'}`} />
                        </button>
                      </div>
                    </div>
                    <p className="text-xs text-gray-500 mb-1">{ds.description}</p>
                    <p className="text-xs text-gray-400 mb-3">{ds.provider} · {ds.interval}</p>
                    {ds.apiInfo && (
                      <div className="mb-3 rounded-lg bg-gray-50 px-3 py-2 text-xs">
                        <p className="font-medium text-gray-500 mb-1">采集接口</p>
                        <p className="text-gray-600 font-mono">{ds.apiInfo.endpoint}</p>
                        {ds.apiInfo.paramsTemplate !== '-' && (
                          <p className="text-gray-500 mt-0.5">参数: {resolveApiParams(ds.apiInfo.paramsTemplate, asset, latestTs)}</p>
                        )}
                        {ds.apiInfo.note && (
                          <p className="text-gray-400 mt-0.5 italic">{ds.apiInfo.note}</p>
                        )}
                      </div>
                    )}
                    <div className="text-xs text-gray-400 space-y-1">
                      {lastTime && <p>最近采集: {dayjs(lastTime).format('YYYY-MM-DD HH:mm')}</p>}
                      {detail && <p>{detail}</p>}
                    </div>
                    <button
                      onClick={() => handleRemove(config)}
                      className="mt-3 text-xs text-red-500 hover:underline"
                    >
                      移除数据源
                    </button>
                  </div>
                );
              })}
              {dsConfigs.length === 0 && (
                <div className="col-span-3 bg-white rounded-xl border border-gray-200 p-5">
                  <p className="text-center text-gray-400 text-sm">暂未绑定数据源，点击上方"添加数据源"进行配置</p>
                </div>
              )}
            </div>
          </div>
        );
      })()}

      {tab === 'contracts' && (
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
      )}
    </div>
  );
}
