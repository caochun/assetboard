import client from './client';
import type { AlarmRule, PageData } from '../types';

export const getAlarmRules = (tenantId: string, page = 0, pageSize = 20) =>
  client.get<PageData<AlarmRule>>('/alarmRule', { params: { tenantId, page, pageSize } });

export const saveAlarmRule = (rule: Partial<AlarmRule>) =>
  client.post<AlarmRule>('/alarmRule', rule);

export const deleteAlarmRule = (id: string) =>
  client.delete(`/alarmRule/${id}`);
