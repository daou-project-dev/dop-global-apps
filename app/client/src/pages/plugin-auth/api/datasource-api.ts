import { apiClient } from '../../../api/client';
import type { Datasource } from '../../../store/types';

export interface CreateDatasourceResponse {
  id: string;
  redirectUrl?: string;
}

export const datasourceApi = {
  create: (datasource: Datasource) =>
    apiClient.post<CreateDatasourceResponse>('/datasources', datasource),

  getAll: () => apiClient.get<Datasource[]>('/datasources'),

  getById: (id: string) => apiClient.get<Datasource>(`/datasources/${id}`),

  update: (id: string, datasource: Partial<Datasource>) =>
    apiClient.put<Datasource>(`/datasources/${id}`, datasource),

  delete: (id: string) => apiClient.delete<void>(`/datasources/${id}`),
};
