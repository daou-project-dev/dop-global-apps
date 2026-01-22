import { useState } from 'react';

import { useMicrosoftGraph, type CreateEventRequest } from '../../hooks/use-microsoft-graph';

import styles from './create-event-form.module.css';

interface CreateEventFormProps {
  calendarId?: string | null;
  onCreated?: () => void;
}

export function CreateEventForm({ calendarId, onCreated }: CreateEventFormProps) {
  const { createEvent, loading, error, clearError } = useMicrosoftGraph();
  const [isOpen, setIsOpen] = useState(false);
  const [formData, setFormData] = useState({
    subject: '',
    location: '',
    startDate: '',
    startTime: '',
    endDate: '',
    endTime: '',
    description: '',
  });

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    clearError();

    const { subject, location, startDate, startTime, endDate, endTime, description } = formData;

    if (!subject || !startDate || !startTime || !endDate || !endTime) {
      alert('필수 항목을 입력해주세요.');
      return;
    }

    const event: CreateEventRequest = {
      subject,
      start: {
        dateTime: `${startDate}T${startTime}:00`,
        timeZone: 'Asia/Seoul',
      },
      end: {
        dateTime: `${endDate}T${endTime}:00`,
        timeZone: 'Asia/Seoul',
      },
    };

    if (location) {
      event.location = { displayName: location };
    }

    if (description) {
      event.body = { contentType: 'text', content: description };
    }

    try {
      await createEvent(event, calendarId ?? undefined);
      alert('이벤트가 생성되었습니다.');
      setFormData({
        subject: '',
        location: '',
        startDate: '',
        startTime: '',
        endDate: '',
        endTime: '',
        description: '',
      });
      setIsOpen(false);
      onCreated?.();
    } catch {
      // error는 hook에서 처리
    }
  };

  if (!isOpen) {
    return (
      <button className={styles.openButton} onClick={() => setIsOpen(true)}>
        + 새 이벤트 만들기
      </button>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h3 className={styles.title}>새 이벤트 만들기</h3>
        <button className={styles.closeButton} onClick={() => setIsOpen(false)}>
          ✕
        </button>
      </div>

      {error && <p className={styles.error}>{error}</p>}

      <form className={styles.form} onSubmit={handleSubmit}>
        <div className={styles.field}>
          <label className={styles.label}>제목 *</label>
          <input
            type="text"
            name="subject"
            value={formData.subject}
            onChange={handleChange}
            className={styles.input}
            placeholder="이벤트 제목"
            required
          />
        </div>

        <div className={styles.field}>
          <label className={styles.label}>장소</label>
          <input
            type="text"
            name="location"
            value={formData.location}
            onChange={handleChange}
            className={styles.input}
            placeholder="장소 (선택)"
          />
        </div>

        <div className={styles.row}>
          <div className={styles.field}>
            <label className={styles.label}>시작 *</label>
            <div className={styles.dateTimeRow}>
              <input
                type="date"
                name="startDate"
                value={formData.startDate}
                onChange={handleChange}
                className={styles.input}
                required
              />
              <input
                type="time"
                name="startTime"
                value={formData.startTime}
                onChange={handleChange}
                className={styles.input}
                required
              />
            </div>
          </div>

          <div className={styles.field}>
            <label className={styles.label}>종료 *</label>
            <div className={styles.dateTimeRow}>
              <input
                type="date"
                name="endDate"
                value={formData.endDate}
                onChange={handleChange}
                className={styles.input}
                required
              />
              <input
                type="time"
                name="endTime"
                value={formData.endTime}
                onChange={handleChange}
                className={styles.input}
                required
              />
            </div>
          </div>
        </div>

        <div className={styles.field}>
          <label className={styles.label}>설명</label>
          <textarea
            name="description"
            value={formData.description}
            onChange={handleChange}
            className={styles.textarea}
            placeholder="이벤트 설명 (선택)"
            rows={3}
          />
        </div>

        <div className={styles.actions}>
          <button
            type="button"
            className={styles.cancelButton}
            onClick={() => setIsOpen(false)}
          >
            취소
          </button>
          <button
            type="submit"
            className={styles.submitButton}
            disabled={loading}
          >
            {loading ? '생성 중...' : '이벤트 생성'}
          </button>
        </div>
      </form>
    </div>
  );
}
