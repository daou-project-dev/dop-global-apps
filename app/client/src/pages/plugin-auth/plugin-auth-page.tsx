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

import styles from './plugin-auth-page.module.css';

const SLACK_PLUGIN_ID = 'slack';

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
  const { formConfig, isLoading, error } = useFetchPluginForm(selectedPluginId);

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
  };

  // 서버에서 폼 데이터 fetch 완료 시 currentPlugin 업데이트
  if (formConfig && currentPlugin.pluginId !== formConfig.pluginId) {
    setCurrentPlugin(formConfig);
  }

  const handleSubmit = () => {
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

    // TODO: 다른 플러그인 저장 API 구현 후 활성화
    alert('저장 기능 준비 중 (Slack만 지원)');
  };

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
            <div className={styles.formWrapper}>
              <FormRenderer onSubmit={handleSubmit} />
            </div>

            <pre className={styles.jsonPreview}>
              {JSON.stringify(currentDatasource, null, 2)}
            </pre>
          </>
        )}
      </div>
    </div>
  );
}
