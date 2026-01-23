import { apiClient } from '../../../api/client';

export interface ExecuteRequest {
  pluginId: string;
  action: string;
  params: Record<string, unknown>;
}

export interface ExecuteResponse {
  success: boolean;
  statusCode: number;
  body: string;
  error: string | null;
}

export interface SlackChannel {
  id: string;
  name: string;
  is_channel: boolean;
  is_private: boolean;
}

export const slackApi = {
  execute: (request: ExecuteRequest) => apiClient.post<ExecuteResponse>('/execute', request),

  getChannels: (externalId: string) =>
    slackApi.execute({
      pluginId: 'slack',
      action: 'conversations.list',
      params: { externalId },
    }),

  postMessage: (externalId: string, channel: string, text: string) =>
    slackApi.execute({
      pluginId: 'slack',
      action: 'chat.postMessage',
      params: { externalId, channel, text },
    }),
};
