import { useState, useEffect } from 'react';
import { useAtom, useSetAtom } from 'jotai';
import clsx from 'clsx';

import {
  currentPluginAtom,
  currentDatasourceAtom,
  switchPluginAtom,
} from '../../store';
import type { Plugin } from '../../store/types';
import { FormRenderer } from '../../components';
import { useFetchPluginForm } from './hooks/use-fetch-plugin-form';
import { pluginApi } from './api/plugin-api';
import { TestFormRenderer } from './components/test-form-renderer';

import styles from './plugin-auth-page.module.css';

const SLACK_PLUGIN_ID = 'slack';
const GOOGLE_CALENDAR_PLUGIN_ID = 'google-calendar';

export function PluginAuthPage() {
  const [currentPlugin, setCurrentPlugin] = useAtom(currentPluginAtom);
  const [currentDatasource] = useAtom(currentDatasourceAtom);
  const switchPlugin = useSetAtom(switchPluginAtom);

  // 플러그인 목록 상태
  const [plugins, setPlugins] = useState<Plugin[]>([]);
  const [pluginsLoading, setPluginsLoading] = useState(true);
  const [pluginsError, setPluginsError] = useState<string | null>(null);

  // 선택된 플러그인의 폼 데이터 fetch
  const [selectedPluginId, setSelectedPluginId] = useState<string | null>(null);
  const { formConfig, testForm, isLoading, error } = useFetchPluginForm(selectedPluginId);

  // Google Calendar 인증 상태
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isAuthenticating, setIsAuthenticating] = useState(false);

  // 플러그인 목록 조회
  useEffect(() => {
    const fetchPlugins = async () => {
      try {
        setPluginsLoading(true);
        const data = await pluginApi.getPlugins();
        setPlugins(data);
      } catch (err) {
        setPluginsError(err instanceof Error ? err.message : '플러그인 목록 조회 실패');
      } finally {
        setPluginsLoading(false);
      }
    };
    fetchPlugins();
  }, []);

  const handlePluginSelect = (plugin: Plugin) => {
    setSelectedPluginId(plugin.pluginId);
    setIsAuthenticated(false); // 플러그인 변경 시 인증 상태 초기화
  };

  // 서버에서 폼 데이터 fetch 완료 시 currentPlugin 업데이트
  useEffect(() => {
    if (formConfig && currentPlugin.pluginId !== formConfig.pluginId) {
      setCurrentPlugin(formConfig);
    }
  }, [formConfig, currentPlugin.pluginId, setCurrentPlugin]);

  const handleSubmit = async () => {
    // Slack OAuth: 팝업으로 인증 진행
    if (currentPlugin.pluginId === SLACK_PLUGIN_ID && currentPlugin.authType === 'oAuth2') {
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

    // Google Calendar: 로컬 ADC 인증 (하드코딩)
    if (currentPlugin.pluginId === GOOGLE_CALENDAR_PLUGIN_ID && currentPlugin.authType === 'serviceAccount') {
      setIsAuthenticating(true);
      try {
        // 로컬 ADC 인증은 백엔드에서 자동 처리되므로 바로 인증 완료 처리
        // 실제로는 백엔드에 인증 테스트 API를 호출할 수 있음
        await new Promise((resolve) => setTimeout(resolve, 500)); // 시뮬레이션
        setIsAuthenticated(true);
      } catch (err) {
        alert('인증 실패: ' + (err instanceof Error ? err.message : '알 수 없는 오류'));
      } finally {
        setIsAuthenticating(false);
      }
      return;
    }

    // TODO: 다른 플러그인 저장 API 구현 후 활성화
    alert('저장 기능 준비 중 (Slack, Google Calendar만 지원)');
  };

  const isGoogleCalendar = currentPlugin.pluginId === GOOGLE_CALENDAR_PLUGIN_ID;

  return (
    <div className={styles.container}>
      <div className={styles.sidebar}>
        <h3 className={styles.sidebarTitle}>Plugins</h3>
        {pluginsLoading && <p className={styles.loading}>로딩 중...</p>}
        {pluginsError && <p className={styles.error}>{pluginsError}</p>}
        {plugins.map((plugin) => (
          <button
            key={plugin.pluginId}
            className={clsx(styles.pluginButton, {
              [styles.pluginButtonActive]: currentPlugin.pluginId === plugin.pluginId,
            })}
            onClick={() => handlePluginSelect(plugin)}
          >
            {plugin.iconUrl && (
              <img
                src={plugin.iconUrl}
                alt={plugin.name}
                className={styles.pluginIcon}
              />
            )}
            {plugin.name}
          </button>
        ))}
      </div>

      <div className={styles.mainContent}>
        <h1 className={styles.header}>Configuring: {currentPlugin.pluginName}</h1>

        {isLoading && <p>Loading plugin configuration...</p>}
        {error && <p style={{ color: 'red' }}>Error: {error}</p>}

        {!isLoading && (
          <>
            {/* 인증 폼 */}
            {!(isGoogleCalendar && isAuthenticated) && (
              <div className={styles.formWrapper}>
                {isGoogleCalendar && (
                  <p className={styles.authInfo}>
                    로컬 ADC 인증을 사용합니다. (gcloud auth application-default login 필요)
                  </p>
                )}
                <FormRenderer onSubmit={handleSubmit} isSubmitting={isAuthenticating} />
              </div>
            )}

            {/* Google Calendar 인증 완료 시 테스트 폼 표시 */}
            {isGoogleCalendar && isAuthenticated && testForm && (
              <div className={styles.testFormWrapper}>
                <p className={styles.authSuccess}>인증 완료</p>
                <TestFormRenderer testForm={testForm} externalId="local-adc" />
              </div>
            )}

            <pre className={styles.jsonPreview}>
              {JSON.stringify(currentDatasource, null, 2)}
            </pre>
          </>
        )}
      </div>
    </div>
  );
}
