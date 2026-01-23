import { apiClient } from '../../../api/client';

import type { Plugin, PluginForm, PluginTestForm } from '../../../store/types';

export const pluginApi = {
  getPlugins: async (): Promise<Plugin[]> => {
    const response = await apiClient.get<Plugin[]>('/plugins');
    return response.data;
  },

  getFormConfig: async (pluginId: string): Promise<PluginForm> => {
    const response = await apiClient.get<PluginForm>(`/plugins/${pluginId}/form-config`);
    return response.data;
  },

  getTestForm: async (pluginId: string): Promise<PluginTestForm> => {
    const response = await apiClient.get<PluginTestForm>(`/plugins/${pluginId}/test-form`);
    return response.data;
  },
};

/** Query Factory */
export const pluginQueries = {
  all: () => ({ queryKey: ['plugins'] as const }),

  list: () => ({
    queryKey: [...pluginQueries.all().queryKey, 'list'] as const,
    queryFn: () => pluginApi.getPlugins(),
  }),

  form: (pluginId: string) => ({
    queryKey: [...pluginQueries.all().queryKey, pluginId, 'form'] as const,
    queryFn: async () => {
      const [formConfig, testForm] = await Promise.all([
        pluginApi.getFormConfig(pluginId),
        pluginApi.getTestForm(pluginId),
      ]);
      return { formConfig, testForm };
    },
  }),
};
