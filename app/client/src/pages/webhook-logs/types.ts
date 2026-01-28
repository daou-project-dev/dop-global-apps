/** 웹훅 이벤트 상태 */
export type WebhookEventStatus = 'RECEIVED' | 'SUCCESS' | 'FAILED';

/** 웹훅 이벤트 로그 */
export interface WebhookEventLog {
  id: number;
  pluginId: string;
  connectionId: number | null;
  eventType: string;
  externalId: string | null;
  status: WebhookEventStatus;
  requestPayload: string;
  responsePayload: string | null;
  errorMessage: string | null;
  createdAt: string;
  processedAt: string | null;
}

/** 웹훅 로그 검색 파라미터 */
export interface WebhookLogSearchParams {
  pluginId?: string;
  connectionId?: number;
  status?: WebhookEventStatus;
}
