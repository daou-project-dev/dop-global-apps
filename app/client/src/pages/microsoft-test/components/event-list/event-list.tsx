import { useState } from 'react';

import { useMicrosoftGraph, type CalendarEvent } from '../../hooks/use-microsoft-graph';

import styles from './event-list.module.css';

interface EventListProps {
  calendarId?: string | null;
  onRefresh?: () => void;
}

export function EventList({ calendarId, onRefresh }: EventListProps) {
  const { getEvents, deleteEvent, loading, error, clearError } = useMicrosoftGraph();
  const [events, setEvents] = useState<CalendarEvent[]>([]);
  const [fetched, setFetched] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const handleFetch = async () => {
    clearError();
    try {
      const data = await getEvents(calendarId ?? undefined);
      setEvents(data);
      setFetched(true);
    } catch {
      // error는 hook에서 처리
    }
  };

  const handleDelete = async (eventId: string) => {
    if (!confirm('이 이벤트를 삭제하시겠습니까?')) return;

    setDeletingId(eventId);
    try {
      await deleteEvent(eventId);
      setEvents((prev) => prev.filter((e) => e.id !== eventId));
      onRefresh?.();
    } catch {
      // error는 hook에서 처리
    } finally {
      setDeletingId(null);
    }
  };

  const formatDateTime = (dateTime: string) => {
    const date = new Date(dateTime);
    return date.toLocaleString('ko-KR', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h3 className={styles.title}>
          이벤트 목록
          {calendarId && <span className={styles.subtitle}>(선택된 캘린더)</span>}
        </h3>
        <button
          className={styles.fetchButton}
          onClick={handleFetch}
          disabled={loading}
        >
          {loading ? '로딩 중...' : fetched ? '새로고침' : '조회'}
        </button>
      </div>

      {error && <p className={styles.error}>{error}</p>}

      {fetched && events.length === 0 && (
        <p className={styles.empty}>이벤트가 없습니다.</p>
      )}

      {events.length > 0 && (
        <ul className={styles.list}>
          {events.map((event) => (
            <li key={event.id} className={styles.item}>
              <div className={styles.eventInfo}>
                <span className={styles.subject}>{event.subject}</span>
                <span className={styles.time}>
                  {event.isAllDay
                    ? '종일'
                    : `${formatDateTime(event.start.dateTime)} - ${formatDateTime(event.end.dateTime)}`}
                </span>
                {event.location?.displayName && (
                  <span className={styles.location}>
                    📍 {event.location.displayName}
                  </span>
                )}
              </div>
              <button
                className={styles.deleteButton}
                onClick={() => handleDelete(event.id)}
                disabled={deletingId === event.id}
              >
                {deletingId === event.id ? '삭제 중...' : '삭제'}
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
