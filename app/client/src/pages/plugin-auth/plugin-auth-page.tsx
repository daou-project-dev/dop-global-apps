import { useState } from 'react';
import { useAtom, useSetAtom } from 'jotai';
import clsx from 'clsx';

import {
  currentPluginAtom,
  currentDatasourceAtom,
  switchPluginAtom,
  S3_FORM,
  GOOGLE_SHEETS_FORM,
  GOOGLE_CALENDAR_FORM,
  SLACK_FORM,
} from '../../store';
import type { PluginForm } from '../../store/types';
import { FormRenderer } from '../../components';
import { TestFormRenderer } from './components/test-form-renderer';
import { useFetchPluginForm } from './hooks/use-fetch-plugin-form';

import styles from './plugin-auth-page.module.css';

const SLACK_PLUGIN_ID = 'slack';

export function PluginAuthPage() {
  const [currentPlugin, setCurrentPlugin] = useAtom(currentPluginAtom);
  const [currentDatasource] = useAtom(currentDatasourceAtom);
  const switchPlugin = useSetAtom(switchPluginAtom);

  // Slack 플러그인 선택 시 서버에서 폼 데이터 fetch
  const [selectedPluginId, setSelectedPluginId] = useState<string | null>(null);
  const { formConfig, testForm, isLoading, error } = useFetchPluginForm(selectedPluginId);

  const handlePluginSelect = (plugin: PluginForm, fetchFromServer = false) => {
    if (fetchFromServer) {
      setSelectedPluginId(plugin.pluginId);
    } else {
      setSelectedPluginId(null);
      switchPlugin(plugin);
    }
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
        <button
          className={clsx(styles.pluginButton, {
            [styles.pluginButtonActive]:
              currentPlugin.pluginId === S3_FORM.pluginId,
          })}
          onClick={() => handlePluginSelect(S3_FORM)}
        >
          Amazon S3
        </button>
        <button
          className={clsx(styles.pluginButton, {
            [styles.pluginButtonActive]:
              currentPlugin.pluginId === GOOGLE_SHEETS_FORM.pluginId,
          })}
          onClick={() => handlePluginSelect(GOOGLE_SHEETS_FORM)}
        >
          Google Sheets (OAuth)
        </button>
        <button
          className={clsx(styles.pluginButton, {
            [styles.pluginButtonActive]:
              currentPlugin.pluginId === GOOGLE_CALENDAR_FORM.pluginId,
          })}
          onClick={() => handlePluginSelect(GOOGLE_CALENDAR_FORM)}
        >
          Google Calendar
        </button>
        <button
          className={clsx(styles.pluginButton, {
            [styles.pluginButtonActive]:
              currentPlugin.pluginId === SLACK_PLUGIN_ID,
          })}
          onClick={() => handlePluginSelect(SLACK_FORM, true)}
        >
          Slack
        </button>
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

            {testForm && <TestFormRenderer testForm={testForm} />}
          </>
        )}
      </div>
    </div>
  );
}
