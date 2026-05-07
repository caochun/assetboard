export interface ApiInfo {
  endpoint: string;
  paramsTemplate: string;
  note?: string;
}

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
  apiInfo?: ApiInfo;
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
    apiInfo: {
      endpoint: '/commonApi/getShipTrack',
      paramsTemplate: 'imo={imo}, btm=滚动24h, etm=当前',
    },
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
    apiInfo: {
      endpoint: '/commonApi/getWeatherByPoint',
      paramsTemplate: 'lon={lon}, lat={lat}',
      note: '坐标取自 AIS 最新位置',
    },
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
    apiInfo: {
      endpoint: '/commonApi/searchShipParticular',
      paramsTemplate: 'imo={imo}',
    },
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
    apiInfo: {
      endpoint: '/valuations/asset-value-history/{imo}',
      paramsTemplate: 'imo={imo}',
    },
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
    apiInfo: {
      endpoint: '/commonApi/getShipArchivePSCHistory',
      paramsTemplate: 'imo={imo}',
    },
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
    apiInfo: {
      endpoint: '/commonApi/getShipAlertList',
      paramsTemplate: '无（全局接口）',
      note: '全局拉取后按资产 IMO 匹配',
    },
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
    apiInfo: {
      endpoint: '-',
      paramsTemplate: '-',
      note: 'Mock 数据，暂无真实 API 集成',
    },
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
    apiInfo: {
      endpoint: '-',
      paramsTemplate: '-',
      note: 'Mock 数据，暂无真实 API 集成',
    },
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
    apiInfo: {
      endpoint: '-',
      paramsTemplate: '-',
      note: 'Mock 数据，暂无真实 API 集成',
    },
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
    apiInfo: {
      endpoint: '-',
      paramsTemplate: '-',
      note: 'Mock 数据，暂无真实 API 集成',
    },
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
    apiInfo: {
      endpoint: '-',
      paramsTemplate: '-',
      note: 'Mock 数据，暂无真实 API 集成',
    },
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
    apiInfo: {
      endpoint: '-',
      paramsTemplate: '-',
      note: 'Mock 数据，暂无真实 API 集成',
    },
  },
];

export const TS_KEY_LABELS: Record<string, string> = {
  lat: '纬度',
  lon: '经度',
  sog: '航速(SOG)',
  cog: '航向(COG)',
  temperature: '温度',
  humidity: '湿度',
  pressure: '气压',
  winddir: '风向',
  windspeed: '风速',
  visibility: '能见度',
  waveheight: '浪高',
  swellheight: '涌浪高度',
  roughValue: '估值',
  valuationCurrency: '估值币种',
  flightHours: '飞行小时',
  cycleCount: '起落次数',
  fuelEfficiency: '燃油效率',
  operatingHours: '运行时长',
  fuelConsumption: '油耗',
};

export function translateTsKey(key: string): string {
  return TS_KEY_LABELS[key] || key;
}

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

export function translateAlarmType(type: string): string {
  const map: Record<string, string> = {
    PSC_DEFICIENCY: 'PSC缺陷',
    NAV_WARNING: '航行警告',
    VALUATION_DROP: '估值下跌',
    MAINTENANCE_DUE: '定检到期',
    FLIGHT_HOURS_EXCEED: '飞行超限',
    EQUIPMENT_OVERUSE: '设备超负荷',
  };
  if (map[type]) return map[type];
  if (type.startsWith('SHIP_ALERT_')) return '船舶预警·' + type.slice('SHIP_ALERT_'.length);
  return type;
}

export const CONDITION_LABELS: Record<string, string> = {
  GT: '大于',
  GTE: '≥',
  LT: '小于',
  LTE: '≤',
  EQ: '等于',
};

export const SEVERITY_LABELS: Record<string, string> = {
  CRITICAL: '严重',
  MAJOR: '重要',
  MINOR: '次要',
  WARNING: '警告',
};

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
