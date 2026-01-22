import { useState, useCallback } from 'react';
import { useMsal } from '@azure/msal-react';
import { InteractionRequiredAuthError } from '@azure/msal-browser';

import { graphScopes, graphEndpoints } from '../config/msal-config';

// Types
export interface UserProfile {
  displayName: string;
  mail: string;
  userPrincipalName: string;
  id: string;
}

export interface Calendar {
  id: string;
  name: string;
  color: string;
  isDefaultCalendar: boolean;
  canEdit: boolean;
}

export interface CalendarEvent {
  id: string;
  subject: string;
  start: { dateTime: string; timeZone: string };
  end: { dateTime: string; timeZone: string };
  location?: { displayName: string };
  isAllDay: boolean;
  organizer?: { emailAddress: { name: string; address: string } };
}

export interface CreateEventRequest {
  subject: string;
  start: { dateTime: string; timeZone: string };
  end: { dateTime: string; timeZone: string };
  location?: { displayName: string };
  body?: { contentType: string; content: string };
}

/**
 * Microsoft Graph API 호출 훅
 */
export function useMicrosoftGraph() {
  const { instance, accounts } = useMsal();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /**
   * Access Token 획득
   */
  const getAccessToken = useCallback(
    async (scopes: string[]) => {
      const account = accounts[0];
      if (!account) {
        throw new Error('로그인이 필요합니다.');
      }

      try {
        // Silent token 획득 시도
        const response = await instance.acquireTokenSilent({
          scopes,
          account,
        });
        return response.accessToken;
      } catch (err) {
        // Silent 실패 시 팝업으로 재시도
        if (err instanceof InteractionRequiredAuthError) {
          const response = await instance.acquireTokenPopup({ scopes });
          return response.accessToken;
        }
        throw err;
      }
    },
    [instance, accounts]
  );

  /**
   * Graph API 호출
   */
  const callGraphApi = useCallback(
    async <T>(
      endpoint: string,
      scopes: string[],
      options?: RequestInit
    ): Promise<T> => {
      setLoading(true);
      setError(null);

      try {
        const accessToken = await getAccessToken(scopes);

        const response = await fetch(endpoint, {
          ...options,
          headers: {
            Authorization: `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
            ...options?.headers,
          },
        });

        if (!response.ok) {
          const errorData = await response.json().catch(() => ({}));
          throw new Error(
            errorData.error?.message || `API 오류: ${response.status}`
          );
        }

        return response.json();
      } catch (err) {
        const message = err instanceof Error ? err.message : 'API 호출 실패';
        setError(message);
        throw err;
      } finally {
        setLoading(false);
      }
    },
    [getAccessToken]
  );

  /**
   * 사용자 프로필 조회
   */
  const getProfile = useCallback(async (): Promise<UserProfile> => {
    return callGraphApi<UserProfile>(graphEndpoints.me, graphScopes.login);
  }, [callGraphApi]);

  /**
   * 캘린더 목록 조회
   */
  const getCalendars = useCallback(async (): Promise<Calendar[]> => {
    const response = await callGraphApi<{ value: Calendar[] }>(
      graphEndpoints.calendars,
      graphScopes.calendarRead
    );
    return response.value;
  }, [callGraphApi]);

  /**
   * 이벤트 목록 조회
   */
  const getEvents = useCallback(
    async (calendarId?: string): Promise<CalendarEvent[]> => {
      const endpoint = calendarId
        ? graphEndpoints.calendarEvents(calendarId)
        : graphEndpoints.events;

      // 최근 30일 ~ 앞으로 30일 이벤트 조회
      const now = new Date();
      const startDate = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
      const endDate = new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000);

      const url = new URL(endpoint);
      url.searchParams.set(
        '$filter',
        `start/dateTime ge '${startDate.toISOString()}' and end/dateTime le '${endDate.toISOString()}'`
      );
      url.searchParams.set('$orderby', 'start/dateTime');
      url.searchParams.set('$top', '50');

      const response = await callGraphApi<{ value: CalendarEvent[] }>(
        url.toString(),
        graphScopes.calendarRead
      );
      return response.value;
    },
    [callGraphApi]
  );

  /**
   * 이벤트 생성
   */
  const createEvent = useCallback(
    async (
      event: CreateEventRequest,
      calendarId?: string
    ): Promise<CalendarEvent> => {
      const endpoint = calendarId
        ? graphEndpoints.calendarEvents(calendarId)
        : graphEndpoints.events;

      return callGraphApi<CalendarEvent>(
        endpoint,
        graphScopes.calendarReadWrite,
        {
          method: 'POST',
          body: JSON.stringify(event),
        }
      );
    },
    [callGraphApi]
  );

  /**
   * 이벤트 삭제
   */
  const deleteEvent = useCallback(
    async (eventId: string): Promise<void> => {
      await callGraphApi<void>(
        `${graphEndpoints.events}/${eventId}`,
        graphScopes.calendarReadWrite,
        { method: 'DELETE' }
      );
    },
    [callGraphApi]
  );

  return {
    loading,
    error,
    getProfile,
    getCalendars,
    getEvents,
    createEvent,
    deleteEvent,
    clearError: () => setError(null),
  };
}
