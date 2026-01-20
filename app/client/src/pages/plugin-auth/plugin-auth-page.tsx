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
import { FormRenderer } from '../../components';
import { datasourceApi } from './api';

import styles from './plugin-auth-page.module.css';

export function PluginAuthPage() {
  const [currentPlugin] = useAtom(currentPluginAtom);
  const [currentDatasource] = useAtom(currentDatasourceAtom);
  const switchPlugin = useSetAtom(switchPluginAtom);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async () => {
    setIsSubmitting(true);
    setError(null);

    try {
      const response = await datasourceApi.create(currentDatasource);

      if (response.data.redirectUrl) {
        window.location.href = response.data.redirectUrl;
        return;
      }

      alert('저장 완료');
    } catch (err) {
      const message = err instanceof Error ? err.message : '저장 실패';
      setError(message);
    } finally {
      setIsSubmitting(false);
    }
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
          onClick={() => switchPlugin(S3_FORM)}
        >
          Amazon S3
        </button>
        <button
          className={clsx(styles.pluginButton, {
            [styles.pluginButtonActive]:
              currentPlugin.pluginId === GOOGLE_SHEETS_FORM.pluginId,
          })}
          onClick={() => switchPlugin(GOOGLE_SHEETS_FORM)}
        >
          Google Sheets (OAuth)
        </button>
        <button
          className={clsx(styles.pluginButton, {
            [styles.pluginButtonActive]:
              currentPlugin.pluginId === GOOGLE_CALENDAR_FORM.pluginId,
          })}
          onClick={() => switchPlugin(GOOGLE_CALENDAR_FORM)}
        >
          Google Calendar
        </button>
        <button
          className={clsx(styles.pluginButton, {
            [styles.pluginButtonActive]:
              currentPlugin.pluginId === SLACK_FORM.pluginId,
          })}
          onClick={() => switchPlugin(SLACK_FORM)}
        >
          Slack
        </button>
      </div>

      <div className={styles.mainContent}>
        <h1 className={styles.header}>Configuring: {currentPlugin.pluginName}</h1>

        <div className={styles.formWrapper}>
          <FormRenderer
            onSubmit={handleSubmit}
            isSubmitting={isSubmitting}
          />

          {error && <p className={styles.errorMessage}>{error}</p>}
        </div>

        <pre className={styles.jsonPreview}>
          {JSON.stringify(currentDatasource, null, 2)}
        </pre>
      </div>
    </div>
  );
}
