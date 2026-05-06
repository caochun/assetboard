import client from './client';
import type { Customer, PageData } from '../types';

export const getCustomers = (tenantId: string, page = 0, pageSize = 20) =>
  client.get<PageData<Customer>>('/customer', { params: { tenantId, page, pageSize } });

export const getCustomerById = (id: string) =>
  client.get<Customer>(`/customer/${id}`);

export const saveCustomer = (customer: Partial<Customer>) =>
  client.post<Customer>('/customer', customer);

export const deleteCustomer = (id: string) =>
  client.delete(`/customer/${id}`);
