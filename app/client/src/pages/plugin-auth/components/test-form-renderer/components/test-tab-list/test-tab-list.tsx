import clsx from 'clsx';

import type { TestTab } from '../../../../../../store/types';

import styles from './test-tab-list.module.css';

interface TestTabListProps {
  tabs: TestTab[];
  activeTabId: string;
  onTabChange: (tabId: string) => void;
}

export function TestTabList({ tabs, activeTabId, onTabChange }: TestTabListProps) {
  return (
    <div className={styles.tabList}>
      {tabs.map((tab) => (
        <button
          key={tab.tabId}
          type="button"
          className={clsx(styles.tab, {
            [styles.tabActive]: tab.tabId === activeTabId,
          })}
          onClick={() => onTabChange(tab.tabId)}
        >
          {tab.tabName}
        </button>
      ))}
    </div>
  );
}
