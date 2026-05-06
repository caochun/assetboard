import client from './client';
import type { DataSourceConfig } from '../types';

export const getDataSourceConfigs = (assetId: string) =>
  client.get<DataSourceConfig[]>('/dataSourceConfig', { params: { assetId } });

export const saveDataSourceConfig = (config: Partial<DataSourceConfig>) =>
  client.post<DataSourceConfig>('/dataSourceConfig', config);

export const deleteDataSourceConfig = (id: string) =>
  client.delete(`/dataSourceConfig/${id}`);
