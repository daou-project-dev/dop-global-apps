import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useAtom } from 'jotai';
import { useEffect, useCallback, useState } from 'react';

import { currentPluginAtom, currentDatasourceAtom } from '../../../store';
import { pluginQueries, connectionApi, connectionQueries } from '../api';

import type { Plugin } from '../../../store/types';

// --- Auth Handlers ---

type AuthHandler = (params: {
  pluginId: string;
  setIsSubmitting: (v: boolean) => void;
  onSuccess: () => void;
  onError: (msg: string) => void;
}) => Promise<void>;

const authHandlers: Record<string, AuthHandler> = {
  oauth2: async ({ pluginId }) => {
    const width = 600;
    const height = 700;
    const left = window.screenX + (window.outerWidth - width) / 2;
    const top = window.screenY + (window.outerHeight - height) / 2;

    window.open(
      `${import.meta.env.VITE_API_BASE_URL}/oauth/${pluginId}/install`,
      'plugin-oauth',
      `width=${width},height=${height},left=${left},top=${top}`
    );
  },

  serviceaccount: async ({ pluginId, setIsSubmitting, onSuccess, onError }) => {
    setIsSubmitting(true);
    try {
      await connectionApi.createConnection({
        pluginId,
        externalId: 'local-adc',
        externalName: 'Local ADC',
      });
      onSuccess();
    } catch (err) {
      onError(err instanceof Error ? err.message : '알 수 없는 오류');
    } finally {
      setIsSubmitting(false);
    }
  },
};

const getAuthHandler = (authType: string | undefined): AuthHandler | null => {
  if (!authType) return null;
  const normalized = authType.toLowerCase().replace('-', '_');
  return authHandlers[normalized] ?? null;
};

// --- Hook ---

export function usePluginAuth() {
  const [currentPlugin, setCurrentPlugin] = useAtom(currentPluginAtom);
  const [currentDatasource] = useAtom(currentDatasourceAtom);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const queryClient = useQueryClient();

  // 플러그인 목록 조회
  const {
    data: plugins = [],
    isLoading: pluginsLoading,
    error: pluginsError,
  } = useQuery(pluginQueries.list());

  // 선택된 플러그인 폼 설정 조회
  const {
    data: formData,
    isLoading: formLoading,
    error: formError,
  } = useQuery({
    ...pluginQueries.form(currentPlugin.pluginId),
    enabled: !!currentPlugin.pluginId,
  });

  // 플러그인 선택 핸들러
  const handlePluginSelect = useCallback(
    (plugin: Plugin) => {
      setCurrentPlugin({
        pluginId: plugin.pluginId,
        pluginName: plugin.name,
        authType: plugin.authType,
        formConfig: [],
      });
    },
    [setCurrentPlugin]
  );

  // formConfig 동기화
  useEffect(() => {
    if (formData?.formConfig && formData.formConfig.pluginId === currentPlugin.pluginId) {
      setCurrentPlugin(formData.formConfig);
    }
  }, [formData?.formConfig, currentPlugin.pluginId, setCurrentPlugin]);

  // 인증 처리
  const handleSubmit = useCallback(async () => {
    const handler = getAuthHandler(currentPlugin.authType);

    if (handler) {
      await handler({
        pluginId: currentPlugin.pluginId,
        setIsSubmitting,
        onSuccess: () => {
          queryClient.invalidateQueries({ queryKey: connectionQueries.all().queryKey });
          alert('인증 완료! 데이터소스 목록에서 확인하세요.');
        },
        onError: (msg) => alert(`인증 실패: ${msg}`),
      });
    } else {
      alert('지원하지 않는 인증 방식입니다.');
    }
  }, [currentPlugin.authType, currentPlugin.pluginId, queryClient]);

  return {
    currentPlugin,
    currentDatasource,
    isSubmitting,
    plugins,
    pluginsLoading,
    pluginsError: pluginsError as Error | null,
    formLoading,
    formError: formError as Error | null,
    handlePluginSelect,
    handleSubmit,
  };
}
