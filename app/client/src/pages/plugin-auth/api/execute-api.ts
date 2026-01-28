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

/**
 * 플러그인 공통 실행 API
 * 모든 플러그인의 액션 실행에 사용
 */
export const executeApi = {
  execute: (request: ExecuteRequest) => apiClient.post<ExecuteResponse>('/execute', request),
};
