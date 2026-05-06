import client from './client';
import type { Alarm, PageData } from '../types';

export const getAlarms = (tenantId: string, page = 0, pageSize = 20) =>
  client.get<PageData<Alarm>>('/alarm', { params: { tenantId, page, pageSize } });

export const acknowledgeAlarm = (alarmId: string) =>
  client.post(`/alarm/${alarmId}/ack`);

export const clearAlarm = (alarmId: string) =>
  client.post(`/alarm/${alarmId}/clear`);

export const getAlarmsByOriginator = (originatorId: string, page = 0, pageSize = 20, originatorType = 4) =>
  client.get<PageData<Alarm>>(`/alarm/originator/${originatorId}`, { params: { page, pageSize, originatorType } });
