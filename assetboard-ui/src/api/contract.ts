import client from './client';
import type { Contract, PageData } from '../types';

export const getContracts = (_tenantId: string, page = 0, pageSize = 20, projectId?: string) =>
  client.get<PageData<Contract>>('/contract', { params: { page, pageSize, projectId } });

export const getContractById = (id: string) =>
  client.get<Contract>(`/contract/${id}`);

export const saveContract = (contract: Partial<Contract>) =>
  client.post<Contract>('/contract', contract);

export const deleteContract = (id: string) =>
  client.delete(`/contract/${id}`);
