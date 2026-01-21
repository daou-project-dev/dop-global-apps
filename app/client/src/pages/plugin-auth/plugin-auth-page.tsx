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
  SLACK_TEST_FORM,
} from '../../store';
import { FormRenderer } from '../../components';
import { TestFormRenderer } from './components/test-form-renderer';

import styles from './plugin-auth-page.module.css';

export function PluginAuthPage() {
  const [currentPlugin] = useAtom(currentPluginAtom);
  const [currentDatasource] = useAtom(currentDatasourceAtom);
  const switchPlugin = useSetAtom(switchPluginAtom);

  const handleSubmit = () => {
    // Slack OAuth: 팝업으로 인증 진행
    if (currentPlugin.pluginId === SLACK_FORM.pluginId && currentPlugin.authType === 'oAuth2') {
      const width = 600;
      const height = 700;
      const left = window.screenX + (window.outerWidth - width) / 2;
      const top = window.screenY + (window.outerHeight - height) / 2;

      window.open(
        `${import.meta.env.VITE_API_BASE_URL}/slack/install`,
        'slack-oauth',
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
          <FormRenderer onSubmit={handleSubmit} />
        </div>

        <pre className={styles.jsonPreview}>
          {JSON.stringify(currentDatasource, null, 2)}
        </pre>

        {currentPlugin.pluginId === SLACK_FORM.pluginId && (
          <TestFormRenderer testForm={SLACK_TEST_FORM} />
        )}
      </div>
    </div>
  );
}
