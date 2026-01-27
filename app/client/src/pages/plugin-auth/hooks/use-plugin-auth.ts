import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useAtom } from 'jotai';
import { useEffect, useCallback, useState } from 'react';

import { currentPluginAtom, currentDatasourceAtom } from '../../../store';
import { pluginQueries, connectionQueries } from '../api';

import type { Plugin, AuthConfig } from '../../../store/types';

// --- Auth Handlers ---

const openOAuthPopup = (url: string) => {
  const width = 600;
  const height = 700;
  const left = window.screenX + (window.outerWidth - width) / 2;
  const top = window.screenY + (window.outerHeight - height) / 2;

  window.open(url, 'plugin-oauth', `width=${width},height=${height},left=${left},top=${top}`);
};

const handleAuthConfig = (authConfig: AuthConfig) => {
  const fullUrl = `${import.meta.env.VITE_API_BASE_URL}${authConfig.url}`;

  if (authConfig.method === 'redirect') {
    openOAuthPopup(fullUrl);
  } else if (authConfig.method === 'submit') {
    // TODO: 자격 증명 제출 폼 처리
    alert('자격 증명 제출 기능은 준비 중입니다.');
  }
};

// --- Hook ---

export function usePluginAuth() {
  const [currentPlugin, setCurrentPlugin] = useAtom(currentPluginAtom);
  const [currentDatasource] = useAtom(currentDatasourceAtom);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [selectedPlugin, setSelectedPlugin] = useState<Plugin | null>(null);
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
      setSelectedPlugin(plugin);
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
    if (!selectedPlugin?.authConfig) {
      alert('지원하지 않는 인증 방식입니다.');
      return;
    }

    setIsSubmitting(true);
    try {
      handleAuthConfig(selectedPlugin.authConfig);

      if (selectedPlugin.authConfig.method === 'redirect') {
        // OAuth는 팝업에서 처리 후 콜백으로 완료
        queryClient.invalidateQueries({ queryKey: connectionQueries.all().queryKey });
      }
    } finally {
      setIsSubmitting(false);
    }
  }, [selectedPlugin, queryClient]);

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
