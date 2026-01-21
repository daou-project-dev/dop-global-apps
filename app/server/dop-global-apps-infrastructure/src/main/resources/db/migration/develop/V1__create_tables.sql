-- ===================================================================
-- DOP Global Apps - 초기 스키마 생성
-- V1: plugin, company, user, plugin_connection, oauth_credential, apikey_credential
-- ===================================================================

-- ===================================================================
-- plugin: 플러그인 마스터 테이블
-- ===================================================================
CREATE TABLE plugin (
    id BIGSERIAL PRIMARY KEY,
    plugin_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    auth_type VARCHAR(20) NOT NULL,
    client_id VARCHAR(200),
    client_secret VARCHAR(500),
    secrets TEXT,
    metadata TEXT,
    icon_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_plugin_status ON plugin(status);

COMMENT ON TABLE plugin IS '플러그인 마스터 테이블';
COMMENT ON COLUMN plugin.plugin_id IS '플러그인 고유 식별자 (slack, google 등)';
COMMENT ON COLUMN plugin.auth_type IS '인증 방식 (OAUTH2, API_KEY, NONE)';
COMMENT ON COLUMN plugin.client_secret IS 'OAuth Client Secret (암호화)';
COMMENT ON COLUMN plugin.secrets IS '추가 민감 정보 JSON (암호화)';
COMMENT ON COLUMN plugin.metadata IS '플러그인 설정 JSON (authUrl, tokenUrl, scopes 등)';

-- ===================================================================
-- company: 고객사 테이블
-- ===================================================================
CREATE TABLE company (
    id BIGSERIAL PRIMARY KEY,
    company_uid VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    domain VARCHAR(200),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    metadata TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_company_status ON company(status);

COMMENT ON TABLE company IS '고객사 테이블';
COMMENT ON COLUMN company.company_uid IS '다우오피스 고객사 UID';
COMMENT ON COLUMN company.status IS '상태 (ACTIVE, INACTIVE, SUSPENDED)';

-- ===================================================================
-- users: 사용자 테이블
-- ===================================================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES company(id),
    platform_user_id VARCHAR(100) NOT NULL,
    login_id VARCHAR(100),
    email VARCHAR(200),
    name VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    metadata TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    UNIQUE(company_id, platform_user_id)
);

CREATE INDEX idx_users_company ON users(company_id);
CREATE INDEX idx_users_status ON users(status);

COMMENT ON TABLE users IS '사용자 테이블';
COMMENT ON COLUMN users.platform_user_id IS '다우오피스 사용자 ID';
COMMENT ON COLUMN users.login_id IS '로그인 ID';
COMMENT ON COLUMN users.status IS '상태 (ACTIVE, INACTIVE)';

-- ===================================================================
-- plugin_connection: 플러그인 연동 테이블
-- ===================================================================
CREATE TABLE plugin_connection (
    id BIGSERIAL PRIMARY KEY,
    plugin_id VARCHAR(50) NOT NULL,
    company_id BIGINT REFERENCES company(id),
    user_id BIGINT REFERENCES users(id),
    scope_type VARCHAR(20) NOT NULL DEFAULT 'WORKSPACE',
    external_id VARCHAR(200) NOT NULL,
    external_name VARCHAR(200),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    metadata TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    UNIQUE(plugin_id, external_id)
);

CREATE INDEX idx_plugin_connection_plugin ON plugin_connection(plugin_id);
CREATE INDEX idx_plugin_connection_company ON plugin_connection(company_id);
CREATE INDEX idx_plugin_connection_status ON plugin_connection(status);

COMMENT ON TABLE plugin_connection IS '플러그인 연동 테이블';
COMMENT ON COLUMN plugin_connection.plugin_id IS '플러그인 식별자';
COMMENT ON COLUMN plugin_connection.scope_type IS '연동 범위 (WORKSPACE, COMPANY, USER)';
COMMENT ON COLUMN plugin_connection.external_id IS '외부 서비스 ID (Slack teamId 등)';
COMMENT ON COLUMN plugin_connection.status IS '상태 (ACTIVE, INACTIVE, REVOKED)';

-- ===================================================================
-- oauth_credential: OAuth 인증 정보 테이블
-- ===================================================================
CREATE TABLE oauth_credential (
    id BIGSERIAL PRIMARY KEY,
    connection_id BIGINT NOT NULL UNIQUE REFERENCES plugin_connection(id) ON DELETE CASCADE,
    access_token TEXT NOT NULL,
    refresh_token TEXT,
    token_type VARCHAR(50) DEFAULT 'Bearer',
    scope TEXT,
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_oauth_credential_connection ON oauth_credential(connection_id);
CREATE INDEX idx_oauth_credential_expires ON oauth_credential(expires_at);

COMMENT ON TABLE oauth_credential IS 'OAuth 인증 정보 테이블';
COMMENT ON COLUMN oauth_credential.access_token IS '액세스 토큰 (암호화)';
COMMENT ON COLUMN oauth_credential.refresh_token IS '리프레시 토큰 (암호화)';
COMMENT ON COLUMN oauth_credential.expires_at IS '토큰 만료 시간';

-- ===================================================================
-- apikey_credential: API Key 인증 정보 테이블
-- ===================================================================
CREATE TABLE apikey_credential (
    id BIGSERIAL PRIMARY KEY,
    connection_id BIGINT NOT NULL UNIQUE REFERENCES plugin_connection(id) ON DELETE CASCADE,
    api_key TEXT NOT NULL,
    api_secret TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_apikey_credential_connection ON apikey_credential(connection_id);

COMMENT ON TABLE apikey_credential IS 'API Key 인증 정보 테이블';
COMMENT ON COLUMN apikey_credential.api_key IS 'API Key (암호화)';
COMMENT ON COLUMN apikey_credential.api_secret IS 'API Secret (암호화)';
