import { Link, useRouterState } from '@tanstack/react-router';
import clsx from 'clsx';

import styles from './gnb.module.css';

import type { GnbProps } from './types';

const NAV_ITEMS = [
  { path: '/', label: '플러그인 인증', icon: '🔐' },
  { path: '/datasources', label: '데이터소스 목록', icon: '📋' },
  { path: '/webhook-logs', label: '웹훅 로그', icon: '🔔' },
] as const;

export function Gnb({ isExpanded, onToggle }: GnbProps) {
  const routerState = useRouterState();
  const currentPath = routerState.location.pathname;

  return (
    <nav className={clsx(styles.gnb, { [styles.collapsed]: !isExpanded })}>
      <div className={styles.header}>
        {isExpanded && <span className={styles.logo}>DOP GLOBAL APPS</span>}
        <button
          className={styles.toggleButton}
          onClick={onToggle}
          aria-label={isExpanded ? '메뉴 접기' : '메뉴 펼치기'}
        >
          {isExpanded ? '◀' : '▶'}
        </button>
      </div>

      <ul className={styles.navList}>
        {NAV_ITEMS.map((item) => (
          <li key={item.path}>
            <Link
              to={item.path}
              className={clsx(styles.navItem, {
                [styles.active]: currentPath === item.path,
              })}
              title={item.label}
            >
              <span className={styles.icon}>{item.icon}</span>
              {isExpanded && <span className={styles.label}>{item.label}</span>}
            </Link>
          </li>
        ))}
      </ul>
    </nav>
  );
}
