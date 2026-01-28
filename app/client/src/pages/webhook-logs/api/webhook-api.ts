import { apiClient } from '../../../api/client';

import type { WebhookEventLog, WebhookLogSearchParams } from '../types';

export const webhookApi = {
  /** 웹훅 로그 목록 조회 */
  getLogs: async (params?: WebhookLogSearchParams): Promise<WebhookEventLog[]> => {
    const searchParams = new URLSearchParams();
    if (params?.pluginId) searchParams.append('pluginId', params.pluginId);
    if (params?.connectionId) searchParams.append('connectionId', String(params.connectionId));
    if (params?.status) searchParams.append('status', params.status);

    const query = searchParams.toString();
    const endpoint = query ? `/webhook/logs?${query}` : '/webhook/logs';

    const response = await apiClient.get<WebhookEventLog[]>(endpoint);
    return response.data;
  },

  /** 웹훅 로그 상세 조회 */
  getLog: async (id: number): Promise<WebhookEventLog> => {
    const response = await apiClient.get<WebhookEventLog>(`/webhook/logs/${id}`);
    return response.data;
  },
};

/** Query Factory */
export const webhookQueries = {
  all: () => ({ queryKey: ['webhookLogs'] as const }),

  list: (params?: WebhookLogSearchParams) => ({
    queryKey: [...webhookQueries.all().queryKey, 'list', params] as const,
    queryFn: () => webhookApi.getLogs(params),
  }),

  detail: (id: number) => ({
    queryKey: [...webhookQueries.all().queryKey, 'detail', id] as const,
    queryFn: () => webhookApi.getLog(id),
  }),
};
