import { useState, useEffect } from 'react';
import clsx from 'clsx';

import type { Plugin, Connection } from '../../store/types';
import { pluginApi } from '../plugin-auth/api/plugin-api';
import { connectionApi } from '../plugin-auth/api/connection-api';
import { TestFormRenderer } from '../plugin-auth/components/test-form-renderer';
import { useFetchPluginForm } from '../plugin-auth/hooks/use-fetch-plugin-form';

import styles from './datasources-page.module.css';

export function DatasourcesPage() {
  // 플러그인 목록
  const [plugins, setPlugins] = useState<Plugin[]>([]);

  // Connection 목록
  const [connections, setConnections] = useState<Connection[]>([]);
  const [connectionsLoading, setConnectionsLoading] = useState(true);
  const [connectionsError, setConnectionsError] = useState<string | null>(null);

  // 선택된 Connection
  const [selectedConnection, setSelectedConnection] = useState<Connection | null>(null);

  // 선택된 Connection의 플러그인 폼 데이터
  const { testForm, isLoading: formLoading } = useFetchPluginForm(
    selectedConnection?.pluginId ?? null
  );

  // 플러그인 목록 조회
  useEffect(() => {
    const fetchPlugins = async () => {
      try {
        const data = await pluginApi.getPlugins();
        setPlugins(data);
      } catch (err) {
        console.error('플러그인 목록 조회 실패:', err);
      }
    };
    fetchPlugins();
  }, []);

  // Connection 목록 조회
  useEffect(() => {
    const fetchConnections = async () => {
      try {
        setConnectionsLoading(true);
        const data = await connectionApi.getConnections();
        setConnections(data);
        // 첫 번째 Connection 자동 선택
        if (data.length > 0) {
          setSelectedConnection(data[0]);
        }
      } catch (err) {
        setConnectionsError(err instanceof Error ? err.message : 'Connection 목록 조회 실패');
      } finally {
        setConnectionsLoading(false);
      }
    };
    fetchConnections();
  }, []);

  const handleConnectionSelect = (connection: Connection) => {
    setSelectedConnection(connection);
  };

  const getPluginForConnection = (connection: Connection) => {
    return plugins.find((p) => p.pluginId === connection.pluginId);
  };

  return (
    <div className={styles.container}>
      <div className={styles.sidebar}>
        <h3 className={styles.sidebarTitle}>연동된 계정</h3>
        {connectionsLoading && <p className={styles.loading}>로딩 중...</p>}
        {connectionsError && <p className={styles.error}>{connectionsError}</p>}
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
                <img
                  src={plugin.iconUrl}
                  alt={plugin.name}
                  className={styles.pluginIcon}
                />
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
            <h1 className={styles.header}>
              {selectedConnection.externalName}
            </h1>
            <p className={styles.subHeader}>
              {getPluginForConnection(selectedConnection)?.name} · {selectedConnection.externalId}
            </p>

            {formLoading && <p>테스트 폼 로딩 중...</p>}
            {testForm && (
              <TestFormRenderer
                testForm={testForm}
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
