export interface PageData<T> {
  data: T[];
  totalPages: number;
  totalElements: number;
  hasNext: boolean;
}

export interface Asset {
  id: string;
  createdTime: number;
  tenantId: string;
  customerId?: string;
  assetProfileId: string;
  name: string;
  type: string;
  label: string;
  status: string;
  additionalInfo?: Record<string, unknown>;
}

export interface Alarm {
  id: string;
  createdTime: number;
  tenantId: string;
  originatorId: string;
  originatorType: string;
  type: string;
  severity: string;
  acknowledged: boolean;
  cleared: boolean;
  startTs: number;
  endTs?: number;
  ackTs?: number;
  clearTs?: number;
  details?: Record<string, unknown>;
}

export interface Customer {
  id: string;
  createdTime: number;
  tenantId: string;
  name: string;
  creditAmount?: number;
  remainingPrincipal?: number;
  address?: string;
  contactInfo?: string;
  additionalInfo?: string;
}

export interface Project {
  id: string;
  createdTime: number;
  tenantId: string;
  customerId?: string;
  name: string;
  projectNo?: string;
  businessType?: string;
  leaseType?: string;
  additionalInfo?: string;
}

export interface Contract {
  id: string;
  createdTime: number;
  projectId?: string;
  contractNo: string;
  amount?: number;
  currency: string;
  lessor?: string;
  lessee?: string;
  status: string;
  signDate?: number;
  additionalInfo?: string;
}

export interface AlarmRule {
  id: string;
  createdTime: number;
  tenantId: string;
  name: string;
  targetType?: string;
  telemetryKey: string;
  condition: string;
  threshold: number;
  severity: string;
  alarmType: string;
  enabled: boolean;
}

export interface TsKvEntry {
  key: string;
  ts: number;
  value: unknown;
  dataType: string;
}

export interface AttributeKvEntry {
  key: string;
  lastUpdateTs: number;
  value: unknown;
  dataType: string;
}

export interface EntityRelation {
  fromId: string;
  fromType: string;
  toId: string;
  toType: string;
  relationType: string;
}

export interface DataSourceConfig {
  id: string;
  createdTime: number;
  assetId: string;
  collectorId: string;
  enabled: boolean;
  params?: string;
}

export interface LoginResponse {
  token: string;
  userId: string;
  email: string;
  name: string;
  authority: string;
}

export interface UserInfo {
  userId: string;
  email: string;
  name: string;
  authority: string;
  tenantId: string;
}
