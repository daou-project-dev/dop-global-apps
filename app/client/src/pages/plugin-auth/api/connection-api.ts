import { apiClient } from '../../../api/client';
import type { Connection } from '../../../store/types';

/**
 * 활성 연동 목록 조회
 */
export const getConnections = async (): Promise<Connection[]> => {
  const response = await apiClient.get<Connection[]>('/connections');
  return response.data;
};

export const connectionApi = {
  getConnections,
};
