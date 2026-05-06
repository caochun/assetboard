import client from './client';
import type { EntityRelation } from '../types';

export const getRelationsFrom = (fromId: string, fromType: string) =>
  client.get<EntityRelation[]>('/relation/from', { params: { fromId, fromType } });

export const getRelationsTo = (toId: string, toType: string) =>
  client.get<EntityRelation[]>('/relation/to', { params: { toId, toType } });
