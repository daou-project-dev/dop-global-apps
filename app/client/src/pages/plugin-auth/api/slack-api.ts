import { apiClient } from '../../../api/client';

export interface SlackExecuteRequest {
  plugin: 'slack';
  method: 'GET' | 'POST';
  uri: string;
  teamId: string;
  body: string;
}

export interface SlackExecuteResponse {
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
  execute: (request: SlackExecuteRequest) =>
    apiClient.post<SlackExecuteResponse>('/execute', request),

  getChannels: (teamId: string) =>
    slackApi.execute({
      plugin: 'slack',
      method: 'GET',
      uri: 'conversations.list',
      teamId,
      body: '{}',
    }),

  postMessage: (teamId: string, channel: string, text: string) =>
    slackApi.execute({
      plugin: 'slack',
      method: 'POST',
      uri: 'chat.postMessage',
      teamId,
      body: JSON.stringify({ channel, text }),
    }),
};
