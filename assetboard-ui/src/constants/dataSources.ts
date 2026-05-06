export interface DataSourceDef {
  id: string;
  name: string;
  fullName: string;
  description: string;
  type: 'timeseries' | 'attribute' | 'alarm';
  assetType?: 'vessel' | 'aircraft' | 'equipment';
  provider: string;
  interval: string;
  tsKeys?: string[];
  alarmTypePatterns?: string[];
}

export const DATA_SOURCES: DataSourceDef[] = [
  // --- 船舶 ---
  {
    id: 'ais',
    name: 'AIS轨迹',
    fullName: 'AIS轨迹(ShipXy)',
    description: '船舶自动识别系统位置与航行数据',
    type: 'timeseries',
    assetType: 'vessel',
    provider: 'ShipXy',
    interval: '每小时',
    tsKeys: ['lat', 'lon', 'sog', 'cog'],
  },
  {
    id: 'weather',
    name: '气象数据',
    fullName: '气象数据(ShipXy)',
    description: '船舶所在位置气象与海况数据',
    type: 'timeseries',
    assetType: 'vessel',
    provider: 'ShipXy',
    interval: '每小时',
    tsKeys: ['temperature', 'humidity', 'pressure', 'winddir', 'windspeed', 'visibility', 'waveheight', 'swellheight'],
  },
  {
    id: 'archive',
    name: '船舶档案',
    fullName: '船舶档案(ShipXy)',
    description: '船舶基本档案与技术参数',
    type: 'attribute',
    assetType: 'vessel',
    provider: 'ShipXy',
    interval: '每日',
  },
  {
    id: 'valuation',
    name: '船舶估值',
    fullName: 'Clarksons估值',
    description: '船舶市场估值数据',
    type: 'timeseries',
    assetType: 'vessel',
    provider: 'Clarksons',
    interval: '每日',
    tsKeys: ['roughValue', 'valuationCurrency'],
  },
  {
    id: 'psc',
    name: 'PSC检查',
    fullName: 'PSC检查(ShipXy)',
    description: '港口国检查缺陷记录',
    type: 'alarm',
    assetType: 'vessel',
    provider: 'ShipXy',
    interval: '每日',
    alarmTypePatterns: ['PSC_DEFICIENCY'],
  },
  {
    id: 'alert',
    name: '船舶预警',
    fullName: '船舶预警(ShipXy)',
    description: '船舶实时预警信息',
    type: 'alarm',
    assetType: 'vessel',
    provider: 'ShipXy',
    interval: '实时',
    alarmTypePatterns: ['SHIP_ALERT_'],
  },
  // --- 飞机 ---
  {
    id: 'aircraft-flight',
    name: '飞行数据',
    fullName: '飞行数据(OEM/ACARS)',
    description: '飞行小时、起落次数、燃油效率等运营数据',
    type: 'timeseries',
    assetType: 'aircraft',
    provider: 'OEM/ACARS',
    interval: '每次航班',
    tsKeys: ['flightHours', 'cycleCount', 'fuelEfficiency'],
  },
  {
    id: 'aircraft-valuation',
    name: '资产估值',
    fullName: '资产估值(内部评估)',
    description: '飞机当前市场估值',
    type: 'timeseries',
    assetType: 'aircraft',
    provider: '内部评估',
    interval: '每月',
    tsKeys: ['roughValue'],
  },
  {
    id: 'aircraft-alarm',
    name: '维修告警',
    fullName: '维修告警(MRO系统)',
    description: '定检到期、飞行小时超限等维修提醒',
    type: 'alarm',
    assetType: 'aircraft',
    provider: 'MRO系统',
    interval: '实时',
    alarmTypePatterns: ['MAINTENANCE_DUE', 'FLIGHT_HOURS_EXCEED'],
  },
  // --- 工程机械 ---
  {
    id: 'equipment-iot',
    name: '设备运行数据',
    fullName: '设备运行数据(IoT终端)',
    description: '运行时长、油耗、GPS定位等实时数据',
    type: 'timeseries',
    assetType: 'equipment',
    provider: 'IoT终端',
    interval: '每小时',
    tsKeys: ['operatingHours', 'fuelConsumption', 'lat', 'lon'],
  },
  {
    id: 'equipment-valuation',
    name: '资产估值',
    fullName: '资产估值(内部评估)',
    description: '工程机械当前市场估值',
    type: 'timeseries',
    assetType: 'equipment',
    provider: '内部评估',
    interval: '每月',
    tsKeys: ['roughValue'],
  },
  {
    id: 'equipment-alarm',
    name: '设备告警',
    fullName: '设备告警(IoT终端)',
    description: '设备超负荷运行、定检到期等告警',
    type: 'alarm',
    assetType: 'equipment',
    provider: 'IoT终端',
    interval: '实时',
    alarmTypePatterns: ['EQUIPMENT_OVERUSE', 'MAINTENANCE_DUE'],
  },
];

export const ASSET_TYPE_LABELS: Record<string, string> = {
  vessel: '船舶',
  aircraft: '飞机',
  equipment: '工程机械',
};

export function getDataSourcesByType(assetType: string): DataSourceDef[] {
  return DATA_SOURCES.filter((d) => (d.assetType || 'vessel') === assetType);
}

export function getDataSourceById(id: string): DataSourceDef | undefined {
  return DATA_SOURCES.find((d) => d.id === id);
}

export function getTimeseriesSource(key: string): string | undefined {
  return DATA_SOURCES.find((d) => d.tsKeys?.includes(key))?.fullName;
}

export function getAlarmSource(alarmType: string): string {
  if (alarmType === 'PSC_DEFICIENCY') return 'PSC检查(ShipXy)';
  if (alarmType.startsWith('SHIP_ALERT_')) return '船舶预警(ShipXy)';
  if (alarmType === 'NAV_WARNING') return '航行警告(ShipXy)';
  if (alarmType === 'VALUATION_DROP') return 'Clarksons估值';
  if (alarmType === 'MAINTENANCE_DUE') return '维修告警(MRO系统)';
  if (alarmType === 'FLIGHT_HOURS_EXCEED') return '维修告警(MRO系统)';
  if (alarmType === 'EQUIPMENT_OVERUSE') return '设备告警(IoT终端)';
  return '系统';
}
