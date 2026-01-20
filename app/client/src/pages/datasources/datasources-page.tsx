import styles from './datasources-page.module.css';

export function DatasourcesPage() {
  return (
    <div className={styles.container}>
      <h1 className={styles.header}>데이터소스 목록</h1>
      <p className={styles.placeholder}>
        인증된 데이터소스 목록이 여기에 표시됩니다.
      </p>
    </div>
  );
}
