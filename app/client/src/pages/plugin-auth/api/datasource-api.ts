import { apiClient } from '../../../api/client';
import type { Datasource } from '../../../store/types';

export interface CreateDatasourceResponse {
  id: string;
  redirectUrl?: string;
}

export const datasourceApi = {
  create: (datasource: Datasource) =>
    apiClient.post<CreateDatasourceResponse>('/api/datasources', datasource),

  getAll: () => apiClient.get<Datasource[]>('/api/datasources'),

  getById: (id: string) => apiClient.get<Datasource>(`/api/datasources/${id}`),

  update: (id: string, datasource: Partial<Datasource>) =>
    apiClient.put<Datasource>(`/api/datasources/${id}`, datasource),

  delete: (id: string) => apiClient.delete<void>(`/api/datasources/${id}`),
};
