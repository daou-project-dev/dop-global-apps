import { useState } from 'react';

import { useMicrosoftGraph, type Calendar } from '../../hooks/use-microsoft-graph';

import styles from './calendar-list.module.css';

interface CalendarListProps {
  onSelectCalendar?: (calendarId: string | null) => void;
  selectedCalendarId?: string | null;
}

export function CalendarList({ onSelectCalendar, selectedCalendarId }: CalendarListProps) {
  const { getCalendars, loading, error, clearError } = useMicrosoftGraph();
  const [calendars, setCalendars] = useState<Calendar[]>([]);
  const [fetched, setFetched] = useState(false);

  const handleFetch = async () => {
    clearError();
    try {
      const data = await getCalendars();
      setCalendars(data);
      setFetched(true);
    } catch {
      // error는 hook에서 처리
    }
  };

  const handleSelect = (calendarId: string | null) => {
    onSelectCalendar?.(calendarId);
  };

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h3 className={styles.title}>캘린더 목록</h3>
        <button
          className={styles.fetchButton}
          onClick={handleFetch}
          disabled={loading}
        >
          {loading ? '로딩 중...' : fetched ? '새로고침' : '조회'}
        </button>
      </div>

      {error && <p className={styles.error}>{error}</p>}

      {fetched && calendars.length === 0 && (
        <p className={styles.empty}>캘린더가 없습니다.</p>
      )}

      {calendars.length > 0 && (
        <ul className={styles.list}>
          <li
            className={`${styles.item} ${selectedCalendarId === null ? styles.selected : ''}`}
            onClick={() => handleSelect(null)}
          >
            <span
              className={styles.colorDot}
              style={{ background: '#666' }}
            />
            <span className={styles.name}>모든 캘린더</span>
          </li>
          {calendars.map((calendar) => (
            <li
              key={calendar.id}
              className={`${styles.item} ${selectedCalendarId === calendar.id ? styles.selected : ''}`}
              onClick={() => handleSelect(calendar.id)}
            >
              <span
                className={styles.colorDot}
                style={{ background: calendar.color || '#0078d4' }}
              />
              <span className={styles.name}>
                {calendar.name}
                {calendar.isDefaultCalendar && (
                  <span className={styles.badge}>기본</span>
                )}
              </span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
