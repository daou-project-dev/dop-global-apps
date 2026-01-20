import { useState } from 'react';
import { Outlet } from '@tanstack/react-router';

import { Gnb } from '../gnb';
import styles from './layout.module.css';

export function Layout() {
  const [isGnbExpanded, setIsGnbExpanded] = useState(true);

  const handleToggleGnb = () => {
    setIsGnbExpanded((prev) => !prev);
  };

  return (
    <div className={styles.layout}>
      <Gnb isExpanded={isGnbExpanded} onToggle={handleToggleGnb} />
      <main className={styles.main}>
        <Outlet />
      </main>
    </div>
  );
}
