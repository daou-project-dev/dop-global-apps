import { apiClient } from '../../../api/client';

import type { Connection } from '../../../store/types';

export const connectionApi = {
  getConnections: async (): Promise<Connection[]> => {
    const response = await apiClient.get<Connection[]>('/connections');
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
