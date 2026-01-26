import { useQuery, useQueryClient } from '@tanstack/react-query';
import clsx from 'clsx';
import { useAtom } from 'jotai';
import { useEffect, useState } from 'react';

import { FormRenderer } from '../../components';
import { currentPluginAtom, currentDatasourceAtom } from '../../store';

import { pluginQueries, connectionApi, connectionQueries } from './api';
import styles from './plugin-auth-page.module.css';

import type { Plugin } from '../../store/types';

const SLACK_PLUGIN_ID = 'slack';
const GOOGLE_CALENDAR_PLUGIN_ID = 'google-calendar';
const MS365_CALENDAR_PLUGIN_ID = 'ms365-calendar';

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

  // 선택된 플러그인의 폼 데이터 fetch (currentPlugin.pluginId 기준)
  const {
    data: formData,
    isLoading,
    error,
  } = useQuery({
    ...pluginQueries.form(currentPlugin.pluginId),
    enabled: !!currentPlugin.pluginId,
  });

  // 플러그인 선택 핸들러 - 기본 정보로 즉시 업데이트
  const handlePluginSelect = (plugin: Plugin) => {
    setCurrentPlugin({
      pluginId: plugin.pluginId,
      pluginName: plugin.name,
      authType: 'oAuth2',
      formConfig: [],
    });
  };

  // API 응답 성공 시 상세 정보(formConfig)로 업데이트
  // pluginId 일치 확인으로 캐시된 이전 데이터가 덮어쓰는 것 방지
  useEffect(() => {
    if (formData?.formConfig && formData.formConfig.pluginId === currentPlugin.pluginId) {
      setCurrentPlugin(formData.formConfig);
    }
  }, [formData?.formConfig, currentPlugin.pluginId, setCurrentPlugin]);

  const openOAuthPopup = () => {
    const width = 600;
    const height = 700;
    const left = window.screenX + (window.outerWidth - width) / 2;
    const top = window.screenY + (window.outerHeight - height) / 2;

    window.open(
      `${import.meta.env.VITE_API_BASE_URL}/oauth/${currentPlugin.pluginId}/install`,
      'plugin-oauth',
      `width=${width},height=${height},left=${left},top=${top}`
    );
  };

  const handleSubmit = async () => {
    const isOAuth2Plugin =
      currentPlugin.authType === 'oAuth2' &&
      (currentPlugin.pluginId === SLACK_PLUGIN_ID ||
        currentPlugin.pluginId === MS365_CALENDAR_PLUGIN_ID);

    if (isOAuth2Plugin) {
      openOAuthPopup();
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
        {!currentPlugin.pluginId ? (
          <div className={styles.emptyState}>
            <p>왼쪽 목록에서 플러그인을 선택하세요.</p>
          </div>
        ) : (
          <>
            <div className={styles.formWrapper}>
              <FormRenderer onSubmit={handleSubmit} isSubmitting={isSubmitting} />
            </div>
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
                  <FormRenderer onSubmit={handleSubmit} />
                </div>

                <pre className={styles.jsonPreview}>
                  {JSON.stringify(currentDatasource, null, 2)}
                </pre>
              </>
            )}
          </>
        )}
      </div>
    </div>
  );
}
