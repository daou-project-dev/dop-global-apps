import { useQuery } from '@tanstack/react-query';
import clsx from 'clsx';
import { useState, useMemo } from 'react';

import { pluginQueries, connectionQueries } from '../plugin-auth/api';
import { TestFormRenderer } from '../plugin-auth/components/test-form-renderer';

import styles from './datasources-page.module.css';

import type { Connection } from '../../store/types';

export function DatasourcesPage() {
  // 플러그인 목록 조회
  const { data: plugins = [] } = useQuery(pluginQueries.list());

  // Connection 목록 조회
  const {
    data: connections = [],
    isLoading: connectionsLoading,
    error: connectionsError,
  } = useQuery(connectionQueries.list());

  // 선택된 Connection ID (null이면 첫 번째 자동 선택)
  const [selectedConnectionId, setSelectedConnectionId] = useState<number | null>(null);

  // 선택된 Connection (ID가 없으면 첫 번째 항목)
  const selectedConnection = useMemo(() => {
    if (selectedConnectionId) {
      return connections.find((c) => c.id === selectedConnectionId) ?? null;
    }
    return connections[0] ?? null;
  }, [connections, selectedConnectionId]);

  // 선택된 Connection의 플러그인 폼 데이터
  const { data: formData, isLoading: formLoading } = useQuery({
    ...pluginQueries.form(selectedConnection?.pluginId ?? ''),
    enabled: !!selectedConnection?.pluginId,
  });

  const handleConnectionSelect = (connection: Connection) => {
    setSelectedConnectionId(connection.id);
  };

  const getPluginForConnection = (connection: Connection) => {
    return plugins.find((p) => p.pluginId === connection.pluginId);
  };

  return (
    <div className={styles.container}>
      <div className={styles.sidebar}>
        <h3 className={styles.sidebarTitle}>연동된 계정</h3>
        {connectionsLoading && <p className={styles.loading}>로딩 중...</p>}
        {connectionsError && (
          <p className={styles.error}>
            {connectionsError instanceof Error
              ? connectionsError.message
              : 'Connection 목록 조회 실패'}
          </p>
        )}
        {!connectionsLoading && connections.length === 0 && (
          <p className={styles.empty}>연동된 계정이 없습니다.</p>
        )}
        {connections.map((connection) => {
          const plugin = getPluginForConnection(connection);
          return (
            <button
              key={connection.id}
              className={clsx(styles.connectionButton, {
                [styles.connectionButtonActive]: selectedConnection?.id === connection.id,
              })}
              onClick={() => handleConnectionSelect(connection)}
            >
              {plugin?.iconUrl && (
                <img src={plugin.iconUrl} alt={plugin.name} className={styles.pluginIcon} />
              )}
              <span className={styles.connectionInfo}>
                <span className={styles.connectionName}>{connection.externalName}</span>
                <span className={styles.connectionPlugin}>{plugin?.name}</span>
              </span>
            </button>
          );
        })}
      </div>

      <div className={styles.mainContent}>
        {selectedConnection ? (
          <>
            <h1 className={styles.header}>{selectedConnection.externalName}</h1>
            <p className={styles.subHeader}>
              {getPluginForConnection(selectedConnection)?.name} · {selectedConnection.externalId}
            </p>

            {formLoading && <p>테스트 폼 로딩 중...</p>}
            {formData?.testForm && (
              <TestFormRenderer
                testForm={formData.testForm}
                externalId={selectedConnection.externalId}
              />
            )}
          </>
        ) : (
          <div className={styles.emptyState}>
            <p>연동된 계정을 선택하세요.</p>
          </div>
        )}
      </div>
    </div>
  );
}
