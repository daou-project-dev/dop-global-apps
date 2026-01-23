import { apiClient } from '../../../api/client';

import type { Connection } from '../../../store/types';

export interface CreateConnectionRequest {
  pluginId: string;
  externalId: string;
  externalName: string;
}

export interface CreateConnectionResponse {
  success: boolean;
  connectionId: number;
}

export const connectionApi = {
  getConnections: async (): Promise<Connection[]> => {
    const response = await apiClient.get<Connection[]>('/connections');
    return response.data;
  },

  createConnection: async (request: CreateConnectionRequest): Promise<CreateConnectionResponse> => {
    const response = await apiClient.post<CreateConnectionResponse>('/connections', request);
    return response.data;
  },
};

/** Query Factory */
export const connectionQueries = {
  all: () => ({ queryKey: ['connections'] as const }),

  list: () => ({
    queryKey: [...connectionQueries.all().queryKey, 'list'] as const,
    queryFn: () => connectionApi.getConnections(),
  }),
};
