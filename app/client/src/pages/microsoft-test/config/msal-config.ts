import type { Configuration } from '@azure/msal-browser';
import { LogLevel } from '@azure/msal-browser';

/**
 * MSAL 설정
 * Azure Portal에서 앱 등록 후 clientId 입력 필요
 */
export const msalConfig: Configuration = {
  auth: {
    // Azure Portal > App registrations > Application (client) ID
    clientId: import.meta.env.VITE_MICROSOFT_CLIENT_ID || 'YOUR_CLIENT_ID',
    // 'common': 모든 계정 (개인 + 조직)
    // 'consumers': 개인 계정만
    // 'organizations': 조직 계정만
    // '{tenant-id}': 특정 테넌트만
    authority: 'https://login.microsoftonline.com/common',
    redirectUri: window.location.origin,
    postLogoutRedirectUri: window.location.origin,
  },
  cache: {
    cacheLocation: 'sessionStorage',
    storeAuthStateInCookie: false,
  },
  system: {
    loggerOptions: {
      loggerCallback: (level, message, containsPii) => {
        if (containsPii) return;
        switch (level) {
          case LogLevel.Error:
            console.error(message);
            break;
          case LogLevel.Warning:
            console.warn(message);
            break;
          case LogLevel.Info:
            console.info(message);
            break;
          case LogLevel.Verbose:
            console.debug(message);
            break;
        }
      },
      logLevel: LogLevel.Warning,
    },
  },
};

/**
 * Microsoft Graph API 권한 (scopes)
 */
export const graphScopes = {
  // 기본 로그인
  login: ['User.Read'],
  // 캘린더 읽기
  calendarRead: ['User.Read', 'Calendars.Read'],
  // 캘린더 읽기/쓰기
  calendarReadWrite: ['User.Read', 'Calendars.ReadWrite'],
};

/**
 * Microsoft Graph API 엔드포인트
 */
export const graphEndpoints = {
  me: 'https://graph.microsoft.com/v1.0/me',
  calendars: 'https://graph.microsoft.com/v1.0/me/calendars',
  events: 'https://graph.microsoft.com/v1.0/me/events',
  calendarEvents: (calendarId: string) =>
    `https://graph.microsoft.com/v1.0/me/calendars/${calendarId}/events`,
};
