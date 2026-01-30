import { useQuery } from '@tanstack/react-query';

import { webhookQueries } from '../../api';

import styles from './webhook-log-detail.module.css';

import type { WebhookLogDetailProps } from './types';

export function WebhookLogDetail({ logId, onClose }: WebhookLogDetailProps) {
  const { data: log, isLoading, error } = useQuery({
    ...webhookQueries.detail(logId),
    enabled: !!logId,
  });

  if (isLoading) {
    return (
      <div className={styles.overlay}>
        <div className={styles.modal}>
          <p>Loading...</p>
        </div>
      </div>
    );
  }

  if (error || !log) {
    return (
      <div className={styles.overlay}>
        <div className={styles.modal}>
          <p className={styles.error}>
            {error instanceof Error ? error.message : 'Failed to load log detail'}
          </p>
          <button type="button" onClick={onClose} className={styles.closeButton}>
            Close
          </button>
        </div>
      </div>
    );
  }

  const formatJson = (str: string | null) => {
    if (!str) return '-';
    try {
      return JSON.stringify(JSON.parse(str), null, 2);
    } catch {
      return str;
    }
  };

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div className={styles.header}>
          <h2 className={styles.title}>Webhook Log Detail</h2>
          <button type="button" onClick={onClose} className={styles.closeIcon}>
            &times;
          </button>
        </div>

        <div className={styles.content}>
          <div className={styles.row}>
            <span className={styles.label}>ID</span>
            <span className={styles.value}>{log.id}</span>
          </div>

          <div className={styles.row}>
            <span className={styles.label}>Plugin ID</span>
            <span className={styles.value}>{log.pluginId}</span>
          </div>

          <div className={styles.row}>
            <span className={styles.label}>Connection ID</span>
            <span className={styles.value}>{log.connectionId ?? '-'}</span>
          </div>

          <div className={styles.row}>
            <span className={styles.label}>Event Type</span>
            <span className={styles.value}>{log.eventType}</span>
          </div>

          <div className={styles.row}>
            <span className={styles.label}>External ID</span>
            <span className={styles.value}>{log.externalId ?? '-'}</span>
          </div>

          <div className={styles.row}>
            <span className={styles.label}>Status</span>
            <span className={`${styles.value} ${styles[`status${log.status}`]}`}>
              {log.status}
            </span>
          </div>

          <div className={styles.row}>
            <span className={styles.label}>Created At</span>
            <span className={styles.value}>{new Date(log.createdAt).toLocaleString()}</span>
          </div>

          <div className={styles.row}>
            <span className={styles.label}>Processed At</span>
            <span className={styles.value}>
              {log.processedAt ? new Date(log.processedAt).toLocaleString() : '-'}
            </span>
          </div>

          {log.errorMessage && (
            <div className={styles.section}>
              <span className={styles.label}>Error Message</span>
              <div className={styles.errorBox}>{log.errorMessage}</div>
            </div>
          )}

          <div className={styles.section}>
            <span className={styles.label}>Request Payload</span>
            <pre className={styles.codeBlock}>{formatJson(log.requestPayload)}</pre>
          </div>

          <div className={styles.section}>
            <span className={styles.label}>Response Payload</span>
            <pre className={styles.codeBlock}>{formatJson(log.responsePayload)}</pre>
          </div>
        </div>

        <div className={styles.footer}>
          <button type="button" onClick={onClose} className={styles.closeButton}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
