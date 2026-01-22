import { useState, useEffect } from 'react';

import type { PluginForm, PluginTestForm } from '../../../store/types';
import { pluginApi } from '../api/plugin-api';

interface PluginFormState {
  formConfig: PluginForm | null;
  testForm: PluginTestForm | null;
  isLoading: boolean;
  error: string | null;
}

/**
 * 플러그인 폼 설정 및 테스트 폼 fetch 훅
 */
export function useFetchPluginForm(pluginId: string | null) {
  const [state, setState] = useState<PluginFormState>({
    formConfig: null,
    testForm: null,
    isLoading: false,
    error: null,
  });

  useEffect(() => {
    if (!pluginId) {
      setState({
        formConfig: null,
        testForm: null,
        isLoading: false,
        error: null,
      });
      return;
    }

    const fetchData = async () => {
      setState((prev) => ({ ...prev, isLoading: true, error: null }));

      try {
        const [formConfig, testForm] = await Promise.all([
          pluginApi.getFormConfig(pluginId),
          pluginApi.getTestForm(pluginId),
        ]);

        setState({
          formConfig,
          testForm,
          isLoading: false,
          error: null,
        });
      } catch (err) {
        setState({
          formConfig: null,
          testForm: null,
          isLoading: false,
          error: err instanceof Error ? err.message : 'Failed to fetch plugin form',
        });
      }
    };

    fetchData();
  }, [pluginId]);

  return state;
}
