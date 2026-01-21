
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
  authType: 'oAuth2' | 'form';
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
