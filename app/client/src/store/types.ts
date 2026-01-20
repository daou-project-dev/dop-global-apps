
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
