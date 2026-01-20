import { atom } from 'jotai';
import { produce } from 'immer';
import _ from 'lodash';
import type { Datasource, PluginForm } from './types';

// --- Mock Data ---

export const GOOGLE_SHEETS_FORM: PluginForm = {
  pluginId: 'google-sheets-plugin',
  pluginName: 'Google Sheets',
  authType: 'oAuth2',
  formConfig: [
    {
      controlType: 'INPUT_TEXT',
      label: 'Scope',
      configProperty: 'datasourceConfiguration.authentication.scope',
      initialValue: 'https://www.googleapis.com/auth/spreadsheets'
    }
  ]
};

export const S3_FORM: PluginForm = {
  pluginId: 's3-plugin-id',
  pluginName: 'Amazon S3',
  authType: 'form',
  formConfig: [
    {
        controlType: 'INPUT_TEXT',
        label: 'Access Key ID',
        configProperty: 'datasourceConfiguration.authentication.username',
    },
    {
        controlType: 'INPUT_TEXT',
        label: 'Secret Access Key',
        configProperty: 'datasourceConfiguration.authentication.password',
        dataType: 'PASSWORD'
    },
    {
        controlType: 'DROP_DOWN',
        label: 'Region',
        configProperty: 'datasourceConfiguration.region',
        options: [
            { label: 'us-east-1', value: 'us-east-1' },
            { label: 'ap-northeast-2', value: 'ap-northeast-2' }
        ]
    }
  ]
};

export const GOOGLE_CALENDAR_FORM: PluginForm = {
  pluginId: 'google-calendar-plugin',
  pluginName: 'Google Calendar',
  authType: 'oAuth2',
  formConfig: [
    {
      label: "Permissions | Scope",
      configProperty: "datasourceConfiguration.authentication.scopeString",
      controlType: "RADIO_BUTTON",
      options: [
        {
          label: "Read Only | View your calendars",
          value: "https://www.googleapis.com/auth/calendar.readonly"
        },
        {
          label: "Read / Write | Manage your calendars",
          value: "https://www.googleapis.com/auth/calendar"
        }
      ],
      initialValue: "https://www.googleapis.com/auth/calendar.readonly"
    }
  ]
};

const INITIAL_DATASOURCE: Datasource = {
    id: 'new-datasource',
    name: 'New Datasource',
    pluginId: '',
    datasourceConfiguration: {}
};

// --- Atoms ---

export const currentPluginAtom = atom<PluginForm>(S3_FORM);
export const currentDatasourceAtom = atom<Datasource>(INITIAL_DATASOURCE);

// --- Write-only atoms / Actions ---

export const switchPluginAtom = atom(
  null,
  (_get, set, plugin: PluginForm) => {
    set(currentPluginAtom, plugin);
    set(currentDatasourceAtom, {
        ...INITIAL_DATASOURCE,
        pluginId: plugin.pluginId,
        datasourceConfiguration: { // Reset config when switching plugins
            authenticationType: plugin.authType
        }
    });
  }
);

export const updateFormValueAtom = atom(
  null,
  (get, set, { path, value }: { path: string, value: any }) => {
    const currentDs = get(currentDatasourceAtom);
    const newDs = produce(currentDs, (draft) => {
       _.set(draft, path, value); 
    });
    set(currentDatasourceAtom, newDs);
  }
);
