import { useState, useEffect } from 'react';
import { useMsal } from '@azure/msal-react';

import { useMicrosoftGraph, type UserProfile } from '../../hooks/use-microsoft-graph';

import styles from './profile-card.module.css';

export function ProfileCard() {
  const { accounts, instance } = useMsal();
  const { getProfile, loading, error } = useMicrosoftGraph();
  const [profile, setProfile] = useState<UserProfile | null>(null);

  const account = accounts[0];

  useEffect(() => {
    if (account && !profile) {
      getProfile()
        .then(setProfile)
        .catch(() => {});
    }
  }, [account, profile, getProfile]);

  const handleLogout = () => {
    instance.logoutPopup();
  };

  if (!account) {
    return null;
  }

  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <div className={styles.avatar}>
          {(profile?.displayName || account.name || '?')[0].toUpperCase()}
        </div>
        <div className={styles.info}>
          <h3 className={styles.name}>{profile?.displayName || account.name}</h3>
          <p className={styles.email}>
            {profile?.mail || profile?.userPrincipalName || account.username}
          </p>
        </div>
      </div>

      {loading && <p className={styles.loading}>프로필 로딩 중...</p>}
      {error && <p className={styles.error}>{error}</p>}

      <button className={styles.logoutButton} onClick={handleLogout}>
        로그아웃
      </button>
    </div>
  );
}
