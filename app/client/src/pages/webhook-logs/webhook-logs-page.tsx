import { useQuery } from '@tanstack/react-query';
import clsx from 'clsx';
import { useState } from 'react';

import { pluginQueries } from '../plugin-auth/api';

import { webhookQueries } from './api';
import { WebhookLogDetail } from './components/webhook-log-detail';
import { WebhookUrlDisplay } from './components/webhook-url-display';
import styles from './webhook-logs-page.module.css';

import type { WebhookLogSearchParams, WebhookEventStatus } from './types';

export function WebhookLogsPage() {
  const [selectedPluginId, setSelectedPluginId] = useState<string>('');
  const [selectedStatus, setSelectedStatus] = useState<WebhookEventStatus | ''>('');
  const [selectedLogId, setSelectedLogId] = useState<number | null>(null);

  // 플러그인 목록 조회
  const { data: plugins = [] } = useQuery(pluginQueries.list());

  // 검색 파라미터 구성
  const searchParams: WebhookLogSearchParams = {};
  if (selectedPluginId) searchParams.pluginId = selectedPluginId;
  if (selectedStatus) searchParams.status = selectedStatus;

  // 웹훅 로그 목록 조회
  const {
    data: logs = [],
    isLoading,
    error,
  } = useQuery(webhookQueries.list(searchParams));

  const statusOptions: { value: WebhookEventStatus | ''; label: string }[] = [
    { value: '', label: 'All Status' },
    { value: 'RECEIVED', label: 'Received' },
    { value: 'SUCCESS', label: 'Success' },
    { value: 'FAILED', label: 'Failed' },
  ];

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Webhook Logs</h1>

      {/* 웹훅 URL 표시 - 플러그인 선택 시 */}
      {selectedPluginId && <WebhookUrlDisplay pluginId={selectedPluginId} />}

      {/* 필터 영역 */}
      <div className={styles.filters}>
        <div className={styles.filterGroup}>
          <label className={styles.filterLabel}>Plugin</label>
          <select
            value={selectedPluginId}
            onChange={(e) => setSelectedPluginId(e.target.value)}
            className={styles.select}
          >
            <option value="">All Plugins</option>
            {plugins.map((plugin) => (
              <option key={plugin.pluginId} value={plugin.pluginId}>
                {plugin.name}
              </option>
            ))}
          </select>
        </div>

        <div className={styles.filterGroup}>
          <label className={styles.filterLabel}>Status</label>
          <select
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value as WebhookEventStatus | '')}
            className={styles.select}
          >
            {statusOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* 로그 테이블 */}
      {isLoading && <p className={styles.loading}>Loading...</p>}
      {error && (
        <p className={styles.error}>
          {error instanceof Error ? error.message : 'Failed to load logs'}
        </p>
      )}

      {!isLoading && !error && (
        <div className={styles.tableWrapper}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>ID</th>
                <th>Plugin</th>
                <th>Event Type</th>
                <th>External ID</th>
                <th>Status</th>
                <th>Created At</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {logs.length === 0 ? (
                <tr>
                  <td colSpan={7} className={styles.emptyRow}>
                    No logs found
                  </td>
                </tr>
              ) : (
                logs.map((log) => (
                  <tr key={log.id}>
                    <td>{log.id}</td>
                    <td>{log.pluginId}</td>
                    <td>{log.eventType}</td>
                    <td>{log.externalId ?? '-'}</td>
                    <td>
                      <span
                        className={clsx(styles.statusBadge, styles[`status${log.status}`])}
                      >
                        {log.status}
                      </span>
                    </td>
                    <td>{new Date(log.createdAt).toLocaleString()}</td>
                    <td>
                      <button
                        type="button"
                        onClick={() => setSelectedLogId(log.id)}
                        className={styles.viewButton}
                      >
                        View
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* 상세 모달 */}
      {selectedLogId && (
        <WebhookLogDetail logId={selectedLogId} onClose={() => setSelectedLogId(null)} />
      )}
    </div>
  );
}
