import { useState } from 'react';

import styles from './webhook-url-display.module.css';

import type { WebhookUrlDisplayProps } from './types';

export function WebhookUrlDisplay({ pluginId }: WebhookUrlDisplayProps) {
  const [copied, setCopied] = useState(false);

  const baseUrl = import.meta.env.VITE_API_BASE_URL || window.location.origin;
  const webhookUrl = `${baseUrl}/webhook/${pluginId}`;

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(webhookUrl);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      // 클립보드 API 미지원 시 fallback
      const textArea = document.createElement('textarea');
      textArea.value = webhookUrl;
      document.body.appendChild(textArea);
      textArea.select();
      document.execCommand('copy');
      document.body.removeChild(textArea);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <div className={styles.container}>
      <label className={styles.label}>Webhook URL</label>
      <div className={styles.urlContainer}>
        <input type="text" value={webhookUrl} readOnly className={styles.urlInput} />
        <button type="button" onClick={handleCopy} className={styles.copyButton}>
          {copied ? 'Copied!' : 'Copy'}
        </button>
      </div>
      <p className={styles.hint}>
        * 위 URL을 {pluginId} 웹훅 설정에 등록하세요
      </p>
    </div>
  );
}
