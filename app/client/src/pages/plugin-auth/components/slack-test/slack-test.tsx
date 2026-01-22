import { useState } from 'react';

import { slackApi } from '../../api';

import styles from './slack-test.module.css';

export function SlackTest() {
  const [teamId, setTeamId] = useState('');
  const [channelId, setChannelId] = useState('');
  const [message, setMessage] = useState('');
  const [result, setResult] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleGetChannels = async () => {
    if (!teamId) {
      setResult('Team ID를 입력하세요');
      return;
    }

    setIsLoading(true);
    setResult(null);

    try {
      const response = await slackApi.getChannels(teamId);
      setResult(JSON.stringify(JSON.parse(response.data.body), null, 2));
    } catch (err) {
      setResult(`Error: ${err instanceof Error ? err.message : '요청 실패'}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSendMessage = async () => {
    if (!teamId || !channelId || !message) {
      setResult('모든 필드를 입력하세요');
      return;
    }

    setIsLoading(true);
    setResult(null);

    try {
      const response = await slackApi.postMessage(teamId, channelId, message);
      setResult(JSON.stringify(response.data, null, 2));
      setMessage('');
    } catch (err) {
      setResult(`Error: ${err instanceof Error ? err.message : '요청 실패'}`);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <h3 className={styles.title}>Slack API 테스트</h3>

      <div className={styles.field}>
        <label className={styles.label}>Team ID</label>
        <input
          type="text"
          className={styles.input}
          placeholder="TXXXXXXXXX"
          value={teamId}
          onChange={(e) => setTeamId(e.target.value)}
        />
      </div>

      <div className={styles.actions}>
        <button className={styles.button} onClick={handleGetChannels} disabled={isLoading}>
          채널 목록 조회
        </button>
      </div>

      <hr className={styles.divider} />

      <div className={styles.field}>
        <label className={styles.label}>Channel ID</label>
        <input
          type="text"
          className={styles.input}
          placeholder="CXXXXXXXXX"
          value={channelId}
          onChange={(e) => setChannelId(e.target.value)}
        />
      </div>

      <div className={styles.field}>
        <label className={styles.label}>메시지</label>
        <input
          type="text"
          className={styles.input}
          placeholder="테스트 메시지"
          value={message}
          onChange={(e) => setMessage(e.target.value)}
        />
      </div>

      <div className={styles.actions}>
        <button className={styles.button} onClick={handleSendMessage} disabled={isLoading}>
          메시지 전송
        </button>
      </div>

      {result && <pre className={styles.result}>{result}</pre>}
    </div>
  );
}
