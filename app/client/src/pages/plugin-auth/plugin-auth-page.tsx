import { useQuery, useQueryClient } from '@tanstack/react-query';
import clsx from 'clsx';
import { useAtom } from 'jotai';
import { useState, useEffect } from 'react';

import { FormRenderer } from '../../components';
import { currentPluginAtom, currentDatasourceAtom } from '../../store';

import { pluginQueries, connectionApi, connectionQueries } from './api';
import styles from './plugin-auth-page.module.css';

import type { Plugin } from '../../store/types';

const SLACK_PLUGIN_ID = 'slack';
const GOOGLE_CALENDAR_PLUGIN_ID = 'google-calendar';

export function PluginAuthPage() {
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

  // 선택된 플러그인의 폼 데이터 fetch
  const [selectedPluginId, setSelectedPluginId] = useState<string | null>(null);
  const {
    data: formData,
    isLoading,
    error,
  } = useQuery({
    ...pluginQueries.form(selectedPluginId!),
    enabled: !!selectedPluginId,
  });

  const handlePluginSelect = (plugin: Plugin) => {
    setSelectedPluginId(plugin.pluginId);
  };

  // 서버에서 폼 데이터 fetch 완료 시 currentPlugin 업데이트
  useEffect(() => {
    if (formData?.formConfig) {
      setCurrentPlugin(formData.formConfig);
    }
  }, [formData?.formConfig, setCurrentPlugin]);

  const handleSubmit = async () => {
    // Slack OAuth: 팝업으로 인증 진행
    if (currentPlugin.authType === 'oAuth2') {
      const width = 600;
      const height = 700;
      const left = window.screenX + (window.outerWidth - width) / 2;
      const top = window.screenY + (window.outerHeight - height) / 2;

      window.open(
        `${import.meta.env.VITE_API_BASE_URL}/oauth/${currentPlugin.pluginId}/install`,
        'plugin-oauth',
        `width=${width},height=${height},left=${left},top=${top}`
      );
      return;
    }

    // Google Calendar: 로컬 ADC 인증 (Service Account)
    if (currentPlugin.pluginId === GOOGLE_CALENDAR_PLUGIN_ID) {
      setIsSubmitting(true);
      try {
        await connectionApi.createConnection({
          pluginId: GOOGLE_CALENDAR_PLUGIN_ID,
          externalId: 'local-adc',
          externalName: 'Local ADC',
        });
        // Connection 목록 캐시 무효화
        queryClient.invalidateQueries({ queryKey: connectionQueries.all().queryKey });
        alert('인증 완료! 데이터소스 목록에서 확인하세요.');
      } catch (err) {
        alert('인증 실패: ' + (err instanceof Error ? err.message : '알 수 없는 오류'));
      } finally {
        setIsSubmitting(false);
      }
      return;
    }

    // TODO: 다른 플러그인 저장 API 구현 후 활성화
    alert('저장 기능 준비 중');
  };

  return (
    <div className={styles.container}>
      <div className={styles.sidebar}>
        <h3 className={styles.sidebarTitle}>Plugins</h3>
        {pluginsLoading && <p className={styles.loading}>로딩 중...</p>}
        {pluginsError && (
          <p className={styles.error}>
            {pluginsError instanceof Error ? pluginsError.message : '플러그인 목록 조회 실패'}
          </p>
        )}
        {plugins.map((plugin) => (
          <button
            key={plugin.pluginId}
            className={clsx(styles.pluginButton, {
              [styles.pluginButtonActive]: currentPlugin.pluginId === plugin.pluginId,
            })}
            onClick={() => handlePluginSelect(plugin)}
          >
            {plugin.iconUrl && (
              <img src={plugin.iconUrl} alt={plugin.name} className={styles.pluginIcon} />
            )}
            {plugin.name}
          </button>
        ))}
      </div>

      <div className={styles.mainContent}>
        <h1 className={styles.header}>Configuring: {currentPlugin.pluginName}</h1>

        {isLoading && <p>Loading plugin configuration...</p>}
        {error && (
          <p style={{ color: 'red' }}>
            Error: {error instanceof Error ? error.message : '폼 설정 조회 실패'}
          </p>
        )}

        {!isLoading && (
          <>
            <div className={styles.formWrapper}>
              <FormRenderer onSubmit={handleSubmit} isSubmitting={isSubmitting} />
            </div>

            <pre className={styles.jsonPreview}>{JSON.stringify(currentDatasource, null, 2)}</pre>
          </>
        )}
      </div>
    </div>
  );
}
