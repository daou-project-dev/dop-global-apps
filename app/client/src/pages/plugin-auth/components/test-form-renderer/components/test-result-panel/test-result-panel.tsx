import clsx from 'clsx';

import styles from './test-result-panel.module.css';

import type { TestResult } from '../../types';

interface TestResultPanelProps {
  result: TestResult;
}

export function TestResultPanel({ result }: TestResultPanelProps) {
  const { success, timestamp, data, error } = result;

  return (
    <div className={styles.panel}>
      <div className={styles.header}>
        <span
          className={clsx(styles.status, {
            [styles.statusSuccess]: success,
            [styles.statusError]: !success,
          })}
        >
          {success ? 'Success' : 'Error'} {success ? '\u2713' : '\u2717'}
        </span>
        <span className={styles.timestamp}>{timestamp}</span>
      </div>
      <pre className={styles.content}>{error ? `Error: ${error}` : data}</pre>
    </div>
  );
}
