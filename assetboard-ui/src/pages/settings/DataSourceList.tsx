import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronDownIcon, ChevronRightIcon } from '@heroicons/react/24/outline';
import { getAssets, getLatestTimeseries, getAttributes } from '../../api/asset';
import { getAlarmsByOriginator } from '../../api/alarm';
import { getDataSourceConfigs } from '../../api/dataSourceConfig';
import { useAuth } from '../../hooks/useAuth';
import { DATA_SOURCES, ASSET_TYPE_LABELS } from '../../constants/dataSources';
import type { DataSourceDef } from '../../constants/dataSources';
import type { Asset, TsKvEntry, AttributeKvEntry, Alarm, DataSourceConfig } from '../../types';
import PageHeader from '../../components/PageHeader';
import dayjs from 'dayjs';

interface AssetDataSnapshot {
  asset: Asset;
  latestTs: TsKvEntry[];
  attributes: AttributeKvEntry[];
  alarms: Alarm[];
  dsConfigs: DataSourceConfig[];
}

const TYPE_BADGE: Record<string, { label: string; cls: string }> = {
  timeseries: { label: '时序', cls: 'bg-blue-100 text-blue-700' },
  attribute: { label: '属性', cls: 'bg-green-100 text-green-700' },
  alarm: { label: '告警', cls: 'bg-amber-100 text-amber-700' },
};

export default function DataSourceList() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [assets, setAssets] = useState<Asset[]>([]);
  const [expandedDs, setExpandedDs] = useState<string | null>(null);
  const [assetSnapshots, setAssetSnapshots] = useState<Record<string, AssetDataSnapshot>>({});
  const [loadingDetail, setLoadingDetail] = useState(false);

  useEffect(() => {
    if (!user) return;
    getAssets(user.tenantId, 0, 100).then(async (r) => {
      const list = r.data.data;
      setAssets(list);
      const snapshots: Record<string, AssetDataSnapshot> = {};
      await Promise.all(
        list.map(async (asset) => {
          const dsRes = await getDataSourceConfigs(asset.id);
          snapshots[asset.id] = {
            asset,
            latestTs: [],
            attributes: [],
            alarms: [],
            dsConfigs: dsRes.data,
          };
        }),
      );
      setAssetSnapshots(snapshots);
    });
  }, [user]);

  const assetsByType = assets.reduce<Record<string, Asset[]>>((acc, a) => {
    acc[a.type] = acc[a.type] || [];
    acc[a.type].push(a);
    return acc;
  }, {});

  const handleExpand = async (ds: DataSourceDef) => {
    if (expandedDs === ds.id) {
      setExpandedDs(null);
      return;
    }
    setExpandedDs(ds.id);

    const boundAssets = getBoundAssets(ds.id);
    const unloaded = boundAssets.filter((a) => !assetSnapshots[a.id]);
    if (unloaded.length === 0) return;

    setLoadingDetail(true);
    const snapshots: Record<string, AssetDataSnapshot> = { ...assetSnapshots };
    await Promise.all(
      unloaded.map(async (asset) => {
        const [tsRes, attrRes, alarmRes, dsRes] = await Promise.all([
          getLatestTimeseries(asset.id),
          getAttributes(asset.id),
          getAlarmsByOriginator(asset.id, 0, 100),
          getDataSourceConfigs(asset.id),
        ]);
        snapshots[asset.id] = {
          asset,
          latestTs: tsRes.data,
          attributes: attrRes.data,
          alarms: alarmRes.data.data,
          dsConfigs: dsRes.data,
        };
      }),
    );
    setAssetSnapshots(snapshots);
    setLoadingDetail(false);
  };

  function getBoundAssets(collectorId: string): Asset[] {
    return assets.filter((a) => {
      const snap = assetSnapshots[a.id];
      if (!snap) return false;
      return snap.dsConfigs.some((c) => c.collectorId === collectorId);
    });
  }

  function getDsStats(ds: DataSourceDef) {
    let total = 0;
    let enabledCount = 0;
    let latestTime: number | undefined;
    let dataCount = 0;

    for (const a of assets) {
      const snap = assetSnapshots[a.id];
      if (!snap) continue;

      const config = snap.dsConfigs.find((c) => c.collectorId === ds.id);
      if (!config) continue;
      total++;
      if (config.enabled) enabledCount++;

      if (ds.type === 'timeseries' && ds.tsKeys) {
        const matching = snap.latestTs.filter((e) => ds.tsKeys!.includes(e.key));
        if (matching.length > 0) {
          dataCount++;
          const t = Math.max(...matching.map((e) => e.ts));
          if (!latestTime || t > latestTime) latestTime = t;
        }
      } else if (ds.type === 'attribute') {
        if (snap.attributes.length > 0) {
          dataCount++;
          const t = Math.max(...snap.attributes.map((x) => x.lastUpdateTs));
          if (!latestTime || t > latestTime) latestTime = t;
        }
      } else if (ds.type === 'alarm' && ds.alarmTypePatterns) {
        const matching = snap.alarms.filter((al) =>
          ds.alarmTypePatterns!.some((p) => al.type === p || al.type.startsWith(p)),
        );
        if (matching.length > 0) {
          dataCount++;
          const t = Math.max(...matching.map((al) => al.createdTime));
          if (!latestTime || t > latestTime) latestTime = t;
        }
      }
    }

    return { total, enabledCount, latestTime, dataCount };
  }

  function getAssetDetail(ds: DataSourceDef, snap: AssetDataSnapshot) {
    const config = snap.dsConfigs.find((c) => c.collectorId === ds.id);
    const enabled = config ? config.enabled : true;
    let lastTime: number | undefined;
    let detail = '';

    if (ds.type === 'timeseries' && ds.tsKeys) {
      const matching = snap.latestTs.filter((e) => ds.tsKeys!.includes(e.key));
      if (matching.length > 0) {
        lastTime = Math.max(...matching.map((e) => e.ts));
        detail = matching.map((e) => `${e.key}=${e.value}`).join(', ');
      }
    } else if (ds.type === 'attribute') {
      if (snap.attributes.length > 0) {
        lastTime = Math.max(...snap.attributes.map((a) => a.lastUpdateTs));
        detail = `${snap.attributes.length} 个属性`;
      }
    } else if (ds.type === 'alarm' && ds.alarmTypePatterns) {
      const matching = snap.alarms.filter((al) =>
        ds.alarmTypePatterns!.some((p) => al.type === p || al.type.startsWith(p)),
      );
      if (matching.length > 0) {
        lastTime = Math.max(...matching.map((al) => al.createdTime));
        const active = matching.filter((al) => !al.cleared).length;
        detail = `${matching.length} 条告警 (${active} 活跃)`;
      }
    }

    return { enabled, lastTime, detail };
  }

  const groups = ['vessel', 'aircraft', 'equipment'] as const;

  return (
    <div>
      <PageHeader title="数据源管理" />

      {groups.map((assetType) => {
        const dsList = DATA_SOURCES.filter((d) => (d.assetType || 'vessel') === assetType);
        const typeAssets = assetsByType[assetType] || [];

        return (
          <div key={assetType} className="mb-8">
            <h2 className="text-sm font-semibold text-gray-900 mb-3 flex items-center gap-2">
              {ASSET_TYPE_LABELS[assetType]}
              <span className="text-xs font-normal text-gray-400">{typeAssets.length} 个资产</span>
            </h2>

            <div className="space-y-3">
              {dsList.map((ds) => {
                const stats = getDsStats(ds);
                const isExpanded = expandedDs === ds.id;
                const badge = TYPE_BADGE[ds.type];

                return (
                  <div key={ds.id} className="bg-white rounded-xl border border-gray-200 overflow-hidden">
                    <button
                      onClick={() => handleExpand(ds)}
                      className="w-full px-5 py-4 flex items-center gap-4 text-left hover:bg-gray-50/50 transition-colors"
                    >
                      {isExpanded
                        ? <ChevronDownIcon className="h-4 w-4 text-gray-400 shrink-0" />
                        : <ChevronRightIcon className="h-4 w-4 text-gray-400 shrink-0" />}

                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 mb-0.5">
                          <span className="text-sm font-medium text-gray-900">{ds.name}</span>
                          <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${badge.cls}`}>
                            {badge.label}
                          </span>
                        </div>
                        <p className="text-xs text-gray-500 truncate">{ds.description}</p>
                      </div>

                      <div className="flex items-center gap-6 shrink-0 text-xs text-gray-500">
                        <div className="text-center">
                          <p className="text-sm font-semibold text-gray-900">{ds.provider}</p>
                          <p>数据来源</p>
                        </div>
                        <div className="text-center">
                          <p className="text-sm font-semibold text-gray-900">{ds.interval}</p>
                          <p>采集频率</p>
                        </div>
                        <div className="text-center">
                          <p className="text-sm font-semibold text-gray-900">{stats.total}</p>
                          <p>关联资产</p>
                        </div>
                        <div className="text-center">
                          <p className="text-sm font-semibold text-gray-900">
                            {ds.tsKeys ? ds.tsKeys.length : ds.alarmTypePatterns ? ds.alarmTypePatterns.length : '-'}
                          </p>
                          <p>监控指标</p>
                        </div>
                      </div>
                    </button>

                    {isExpanded && (
                      <div className="border-t border-gray-100 px-5 py-4">
                        {/* Config info */}
                        <div className="mb-4 grid grid-cols-2 gap-x-8 gap-y-2 text-xs">
                          <div>
                            <span className="text-gray-400">数据源全称：</span>
                            <span className="text-gray-700">{ds.fullName}</span>
                          </div>
                          <div>
                            <span className="text-gray-400">数据类型：</span>
                            <span className="text-gray-700">{ds.type === 'timeseries' ? '时序数据' : ds.type === 'attribute' ? '属性数据' : '告警数据'}</span>
                          </div>
                          {ds.tsKeys && (
                            <div className="col-span-2">
                              <span className="text-gray-400">监控指标：</span>
                              <span className="text-gray-700 font-mono">{ds.tsKeys.join(', ')}</span>
                            </div>
                          )}
                          {ds.alarmTypePatterns && (
                            <div className="col-span-2">
                              <span className="text-gray-400">告警类型：</span>
                              <span className="text-gray-700 font-mono">{ds.alarmTypePatterns.join(', ')}</span>
                            </div>
                          )}
                        </div>

                        {/* Asset table */}
                        <h3 className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">关联资产采集详情</h3>
                        {loadingDetail ? (
                          <div className="py-4 text-center text-gray-400 text-sm">加载中...</div>
                        ) : (
                          <table className="w-full text-sm">
                            <thead>
                              <tr className="text-left text-xs font-semibold text-gray-500 uppercase tracking-wider border-b border-gray-100">
                                <th className="pb-2 pr-4">资产名称</th>
                                <th className="pb-2 pr-4">启用状态</th>
                                <th className="pb-2 pr-4">最近采集</th>
                                <th className="pb-2">数据概览</th>
                              </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-50">
                              {getBoundAssets(ds.id).map((asset) => {
                                const snap = assetSnapshots[asset.id];
                                if (!snap) return (
                                  <tr key={asset.id}>
                                    <td className="py-2 text-gray-900">{asset.name}</td>
                                    <td colSpan={3} className="py-2 text-gray-400">加载中...</td>
                                  </tr>
                                );

                                const info = getAssetDetail(ds, snap);
                                return (
                                  <tr
                                    key={asset.id}
                                    className="cursor-pointer hover:bg-blue-50/40 transition-colors"
                                    onClick={() => navigate(`/assets/${asset.id}`)}
                                  >
                                    <td className="py-2 pr-4 font-medium text-gray-900">{asset.name}</td>
                                    <td className="py-2 pr-4">
                                      <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${info.enabled ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                                        {info.enabled ? '已启用' : '已停用'}
                                      </span>
                                    </td>
                                    <td className="py-2 pr-4 text-gray-500">
                                      {info.lastTime ? dayjs(info.lastTime).format('YYYY-MM-DD HH:mm') : '-'}
                                    </td>
                                    <td className="py-2 text-gray-500 font-mono text-xs truncate max-w-[300px]">
                                      {info.detail || '暂无数据'}
                                    </td>
                                  </tr>
                                );
                              })}
                              {getBoundAssets(ds.id).length === 0 && (
                                <tr><td colSpan={4} className="py-4 text-center text-gray-400">暂无关联资产</td></tr>
                              )}
                            </tbody>
                          </table>
                        )}
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        );
      })}
    </div>
  );
}
