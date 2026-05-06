import client from './client';
import type { LoginResponse, UserInfo } from '../types';

export const login = (email: string, password: string) =>
  client.post<LoginResponse>('/auth/login', { email, password });

export const getCurrentUser = () =>
  client.get<UserInfo>('/auth/user');
