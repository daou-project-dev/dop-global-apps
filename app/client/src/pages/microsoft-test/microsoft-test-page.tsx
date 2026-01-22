import { useState, useEffect } from 'react';
import { MsalProvider, useMsal, useIsAuthenticated } from '@azure/msal-react';
import { PublicClientApplication, BrowserAuthError } from '@azure/msal-browser';

import { msalConfig, graphScopes } from './config/msal-config';
import { ProfileCard } from './components/profile-card';
import { CalendarList } from './components/calendar-list';
import { EventList } from './components/event-list';
import { CreateEventForm } from './components/create-event-form';

import styles from './microsoft-test-page.module.css';

/**
 * MSAL interaction 상태 클리어
 */
function clearMsalInteractionState() {
  const keys = Object.keys(sessionStorage);
  keys.forEach((key) => {
    if (key.includes('msal') && key.includes('interaction')) {
      sessionStorage.removeItem(key);
    }
  });
}

// MSAL 인스턴스 생성
const msalInstance = new PublicClientApplication(msalConfig);

// 초기화 Promise
const msalInitPromise = msalInstance.initialize().then(() => {
  // 리다이렉트 응답 처리
  return msalInstance.handleRedirectPromise();
}).catch(() => {
  // 초기화 실패 시 interaction 상태 클리어
  clearMsalInteractionState();
});

/**
 * 로그인 버튼 컴포넌트
 */
function LoginButton() {
  const { instance } = useMsal();
  const [loading, setLoading] = useState(false);

  const handleLogin = async () => {
    setLoading(true);
    try {
      // redirect 방식 사용 (팝업 차단 문제 회피)
      await instance.loginRedirect({
        scopes: ['User.Read', 'Calendars.Read', 'Calendars.ReadWrite'],
        prompt: 'consent',
      });
    } catch (error) {
      // interaction_in_progress 에러 처리
      if (error instanceof BrowserAuthError && error.errorCode === 'interaction_in_progress') {
        clearMsalInteractionState();
        // 재시도
        try {
          await instance.loginRedirect({
            scopes: ['User.Read', 'Calendars.Read', 'Calendars.ReadWrite'],
            prompt: 'consent',
          });
        } catch (retryError) {
          console.error('로그인 재시도 실패:', retryError);
        }
      } else {
        console.error('로그인 실패:', error);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.loginContainer}>
      <div className={styles.loginCard}>
        <div className={styles.microsoftLogo}>
          <svg viewBox="0 0 21 21" width="48" height="48">
            <rect x="1" y="1" width="9" height="9" fill="#f25022" />
            <rect x="11" y="1" width="9" height="9" fill="#7fba00" />
            <rect x="1" y="11" width="9" height="9" fill="#00a4ef" />
            <rect x="11" y="11" width="9" height="9" fill="#ffb900" />
          </svg>
        </div>
        <h1 className={styles.loginTitle}>Microsoft Graph API 테스트</h1>
        <p className={styles.loginDescription}>
          Microsoft 계정으로 로그인하여 캘린더 API를 테스트합니다.
        </p>
        <button
          className={styles.loginButton}
          onClick={handleLogin}
          disabled={loading}
        >
          {loading ? '로그인 중...' : 'Microsoft로 로그인'}
        </button>
        <p className={styles.loginNote}>
          개인 계정 (Outlook.com) 또는 회사/학교 계정 모두 사용 가능
        </p>
      </div>
    </div>
  );
}

/**
 * 메인 대시보드 컴포넌트
 */
function Dashboard() {
  const [selectedCalendarId, setSelectedCalendarId] = useState<string | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);

  const handleRefresh = () => {
    setRefreshKey((prev) => prev + 1);
  };

  return (
    <div className={styles.dashboard}>
      <header className={styles.header}>
        <h1 className={styles.title}>Microsoft Calendar API 테스트</h1>
      </header>

      <div className={styles.content}>
        <aside className={styles.sidebar}>
          <ProfileCard />
          <CalendarList
            selectedCalendarId={selectedCalendarId}
            onSelectCalendar={setSelectedCalendarId}
          />
        </aside>

        <main className={styles.main}>
          <CreateEventForm
            calendarId={selectedCalendarId}
            onCreated={handleRefresh}
          />
          <EventList
            key={refreshKey}
            calendarId={selectedCalendarId}
            onRefresh={handleRefresh}
          />
        </main>
      </div>
    </div>
  );
}

/**
 * 인증 상태에 따른 분기 컴포넌트
 */
function AuthenticatedContent() {
  const isAuthenticated = useIsAuthenticated();

  if (!isAuthenticated) {
    return <LoginButton />;
  }

  return <Dashboard />;
}

/**
 * Microsoft Test 페이지
 */
export function MicrosoftTestPage() {
  const [isInitialized, setIsInitialized] = useState(false);

  useEffect(() => {
    msalInitPromise.then(() => {
      setIsInitialized(true);
    });
  }, []);

  if (!isInitialized) {
    return (
      <div className={styles.container}>
        <div className={styles.loginContainer}>
          <p>MSAL 초기화 중...</p>
        </div>
      </div>
    );
  }

  return (
    <MsalProvider instance={msalInstance}>
      <div className={styles.container}>
        <AuthenticatedContent />
      </div>
    </MsalProvider>
  );
}
