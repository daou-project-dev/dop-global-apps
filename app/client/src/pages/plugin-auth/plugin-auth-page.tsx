import clsx from 'clsx';

import { FormRenderer } from '../../components';

import { usePluginAuth } from './hooks';
import styles from './plugin-auth-page.module.css';

import type { Plugin, PluginForm } from '../../store/types';

// --- Sub Components ---

interface PluginSidebarProps {
  plugins: Plugin[];
  isLoading: boolean;
  error: Error | null;
  selectedPluginId: string;
  onSelect: (plugin: Plugin) => void;
}

function PluginSidebar({ plugins, isLoading, error, selectedPluginId, onSelect }: PluginSidebarProps) {
  return (
    <div className={styles.sidebar}>
      <h3 className={styles.sidebarTitle}>Plugins</h3>
      {isLoading && <p className={styles.loading}>로딩 중...</p>}
      {error && (
        <p className={styles.error}>
          {error.message || '플러그인 목록 조회 실패'}
        </p>
      )}
      {plugins.map((plugin) => (
        <button
          key={plugin.pluginId}
          className={clsx(styles.pluginButton, {
            [styles.pluginButtonActive]: selectedPluginId === plugin.pluginId,
          })}
          onClick={() => onSelect(plugin)}
        >
          {plugin.iconUrl && (
            <img src={plugin.iconUrl} alt={plugin.name} className={styles.pluginIcon} />
          )}
          {plugin.name}
        </button>
      ))}
    </div>
  );
}

interface PluginConfigContentProps {
  currentPlugin: PluginForm;
  currentDatasource: unknown;
  isLoading: boolean;
  error: Error | null;
  onSubmit: () => void;
}

function PluginConfigContent({
  currentPlugin,
  currentDatasource,
  isLoading,
  error,
  onSubmit,
}: PluginConfigContentProps) {
  if (!currentPlugin.pluginId) {
    return (
      <div className={styles.emptyState}>
        <p>왼쪽 목록에서 플러그인을 선택하세요.</p>
      </div>
    );
  }

  return (
    <>
      <h1 className={styles.header}>Configuring: {currentPlugin.pluginName}</h1>

      {isLoading && <p>Loading plugin configuration...</p>}
      {error && (
        <p style={{ color: 'red' }}>
          Error: {error.message || '폼 설정 조회 실패'}
        </p>
      )}

      {!isLoading && (
        <>
          <div className={styles.formWrapper}>
            <FormRenderer onSubmit={onSubmit} />
          </div>

          <pre className={styles.jsonPreview}>
            {JSON.stringify(currentDatasource, null, 2)}
          </pre>
        </>
      )}
    </>
  );
}

// --- Main Component ---

export function PluginAuthPage() {
  const {
    currentPlugin,
    currentDatasource,
    plugins,
    pluginsLoading,
    pluginsError,
    formLoading,
    formError,
    handlePluginSelect,
    handleSubmit,
  } = usePluginAuth();

  return (
    <div className={styles.container}>
      <PluginSidebar
        plugins={plugins}
        isLoading={pluginsLoading}
        error={pluginsError}
        selectedPluginId={currentPlugin.pluginId}
        onSelect={handlePluginSelect}
      />

      <div className={styles.mainContent}>
        <PluginConfigContent
          currentPlugin={currentPlugin}
          currentDatasource={currentDatasource}
          isLoading={formLoading}
          error={formError}
          onSubmit={handleSubmit}
        />
      </div>
    </div>
  );
}
