-- ===================================================================
-- DOP Global Apps - 웹훅 테이블 생성
-- V2: webhook_subscription, webhook_event_log
-- ===================================================================

-- ===================================================================
-- webhook_subscription: 웹훅 구독 테이블
-- ===================================================================
CREATE TABLE webhook_subscription (
    id BIGSERIAL PRIMARY KEY,
    plugin_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(100),
    connection_id BIGINT REFERENCES plugin_connection(id),
    target_type VARCHAR(20) NOT NULL,
    target_url VARCHAR(500),
    target_method VARCHAR(100),
    filter_expr TEXT,
    retry_policy TEXT,
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_webhook_sub_plugin_event ON webhook_subscription(plugin_id, event_type);
CREATE INDEX idx_webhook_sub_connection ON webhook_subscription(connection_id);
CREATE INDEX idx_webhook_sub_enabled ON webhook_subscription(enabled);

COMMENT ON TABLE webhook_subscription IS '웹훅 구독 테이블';
COMMENT ON COLUMN webhook_subscription.plugin_id IS '플러그인 ID (slack, jira 등)';
COMMENT ON COLUMN webhook_subscription.event_type IS '이벤트 타입 (null이면 전체)';
COMMENT ON COLUMN webhook_subscription.connection_id IS '특정 연동만 (null이면 전체)';
COMMENT ON COLUMN webhook_subscription.target_type IS '디스패치 대상 (HTTP, INTERNAL)';
COMMENT ON COLUMN webhook_subscription.target_url IS 'HTTP 호출 URL';
COMMENT ON COLUMN webhook_subscription.target_method IS '내부 메서드 (ServiceName.methodName)';
COMMENT ON COLUMN webhook_subscription.filter_expr IS 'JSONPath 필터 표현식';
COMMENT ON COLUMN webhook_subscription.retry_policy IS '재시도 정책 JSON';

-- ===================================================================
-- webhook_event_log: 웹훅 이벤트 로그 테이블
-- ===================================================================
CREATE TABLE webhook_event_log (
    id BIGSERIAL PRIMARY KEY,
    plugin_id VARCHAR(50) NOT NULL,
    connection_id BIGINT REFERENCES plugin_connection(id),
    event_type VARCHAR(100),
    external_id VARCHAR(100),
    payload TEXT,
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    processed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_webhook_log_plugin ON webhook_event_log(plugin_id, created_at DESC);
CREATE INDEX idx_webhook_log_connection ON webhook_event_log(connection_id);
CREATE INDEX idx_webhook_log_status ON webhook_event_log(status);
CREATE INDEX idx_webhook_log_created ON webhook_event_log(created_at DESC);

COMMENT ON TABLE webhook_event_log IS '웹훅 이벤트 로그 테이블';
COMMENT ON COLUMN webhook_event_log.plugin_id IS '플러그인 ID';
COMMENT ON COLUMN webhook_event_log.connection_id IS '연동 ID (식별된 경우)';
COMMENT ON COLUMN webhook_event_log.event_type IS '이벤트 타입';
COMMENT ON COLUMN webhook_event_log.external_id IS '외부 시스템 ID (team_id, cloudId 등)';
COMMENT ON COLUMN webhook_event_log.payload IS '원본 페이로드';
COMMENT ON COLUMN webhook_event_log.status IS '처리 상태 (RECEIVED, SUCCESS, FAILED)';
COMMENT ON COLUMN webhook_event_log.error_message IS '에러 메시지';
COMMENT ON COLUMN webhook_event_log.processed_at IS '처리 완료 시간';
