import { apiClient } from '../../../api/client';
import type { PluginForm, PluginTestForm } from '../../../store/types';

/**
 * 플러그인 폼 설정 조회
 */
export const getFormConfig = async (pluginId: string): Promise<PluginForm> => {
  const response = await apiClient.get<PluginForm>(`/plugins/${pluginId}/form-config`);
  return response.data;
};

/**
 * 플러그인 테스트 폼 조회
 */
export const getTestForm = async (pluginId: string): Promise<PluginTestForm> => {
  const response = await apiClient.get<PluginTestForm>(`/plugins/${pluginId}/test-form`);
  return response.data;
};

export const pluginApi = {
  getFormConfig,
  getTestForm,
};
