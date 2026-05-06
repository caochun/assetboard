import client from './client';
import type { Asset, PageData, TsKvEntry, AttributeKvEntry } from '../types';

export const getAssets = (tenantId: string, page = 0, pageSize = 20, customerId?: string) =>
  client.get<PageData<Asset>>('/asset', { params: { tenantId, page, pageSize, customerId } });

export const getAssetById = (id: string) =>
  client.get<Asset>(`/asset/${id}`);

export const getTimeseries = (entityId: string, key: string, startTs: number, endTs: number, limit = 1000) =>
  client.get<TsKvEntry[]>(`/plugins/telemetry/${entityId}/timeseries`, { params: { key, startTs, endTs, limit } });

export const getLatestTimeseries = (entityId: string) =>
  client.get<TsKvEntry[]>(`/plugins/telemetry/${entityId}/timeseries/latest`);

export const getAttributes = (entityId: string) =>
  client.get<AttributeKvEntry[]>(`/plugins/telemetry/${entityId}/attributes`);
