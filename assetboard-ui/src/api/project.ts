import client from './client';
import type { Project, PageData } from '../types';

export const getProjects = (tenantId: string, page = 0, pageSize = 20) =>
  client.get<PageData<Project>>('/project', { params: { tenantId, page, pageSize } });

export const saveProject = (project: Partial<Project>) =>
  client.post<Project>('/project', project);

export const getProjectById = (id: string) =>
  client.get<Project>(`/project/${id}`);

export const getProjectsByCustomerId = (customerId: string, page = 0, pageSize = 20) =>
  client.get<PageData<Project>>('/project', { params: { customerId, page, pageSize } });

export const deleteProject = (id: string) =>
  client.delete(`/project/${id}`);
