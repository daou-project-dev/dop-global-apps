// --- Plugin Types ---

/** 인증 타입 */
export type AuthType = 'OAUTH2' | 'SERVICE_ACCOUNT' | 'API_KEY' | 'BASIC';

/** 플러그인 상태 */
export type PluginStatus = 'ACTIVE' | 'INACTIVE';

/** 플러그인 목록 조회 응답 */
export interface Plugin {
  pluginId: string;
  name: string;
  description: string;
  iconUrl: string;
  authType: AuthType;
  status: PluginStatus;
}

/** 연동 범위 타입 */
export type ScopeType = 'WORKSPACE' | 'USER';

/** 연동 상태 */
export type ConnectionStatus = 'ACTIVE' | 'REVOKED';

/** Connection 목록 조회 응답 */
export interface Connection {
  id: number;
  pluginId: string;
  externalId: string;
  externalName: string;
  scopeType: ScopeType;
  status: ConnectionStatus;
  createdAt: string;
}

// --- Form Types ---

export type ControlType = 'INPUT_TEXT' | 'DROP_DOWN' | 'CHECKBOX' | 'RADIO_BUTTON';

export interface ControlProps {
  controlType: ControlType;
  label: string;
  configProperty: string;
  dataType?: 'PASSWORD' | 'TEXT';
  initialValue?: any;
  options?: { label: string; value: string }[]; // For Dropdown
  hidden?: boolean;
}

export interface DatasourceConfig {
  authenticationType?: 'oAuth2' | 'base' | 'form';
  [key: string]: any;
}

export interface Datasource {
  id: string;
  name: string;
  pluginId: string;
  datasourceConfiguration: DatasourceConfig;
}

export interface PluginForm {
  pluginId: string;
  pluginName: string;
  authType: AuthType | string; // AuthType 또는 백엔드 응답값
  formConfig: ControlProps[]; // The "form.json"
}

// --- Test Form Types ---

/** 테스트 컨트롤 타입 */
export type TestControlType = ControlType | 'TEXTAREA';

/** 테스트 폼 컨트롤 */
export interface TestControlProps {
  controlType: TestControlType;
  label: string;
  name: string; // 입력값 저장 키
  placeholder?: string;
  required?: boolean;
  initialValue?: string;
  options?: { label: string; value: string }[];
}

/** API 실행 설정 */
export interface TestApiConfig {
  method: 'GET' | 'POST';
  uri: string; // conversations.list, chat.postMessage 등
  bodyTemplate?: string; // "{ \"channel\": \"{{channel}}\" }"
}

/** 테스트 탭 */
export interface TestTab {
  tabId: string;
  tabName: string;
  description?: string;
  controls: TestControlProps[];
  api: TestApiConfig;
}

/** 플러그인 테스트 폼 */
export interface PluginTestForm {
  pluginId: string;
  tabs: TestTab[];
}
