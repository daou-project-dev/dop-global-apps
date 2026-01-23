import { produce } from 'immer';
import { atom } from 'jotai';
import _ from 'lodash';

import { currentPluginAtom } from '../plugin';

import type { Datasource, PluginForm } from '../types';

// --- Initial State ---

const INITIAL_DATASOURCE: Datasource = {
  id: 'new-datasource',
  name: 'New Datasource',
  pluginId: '',
  datasourceConfiguration: {},
};

// --- Atoms ---

export const currentDatasourceAtom = atom<Datasource>(INITIAL_DATASOURCE);

// --- Actions ---

export const switchPluginAtom = atom(null, (_get, set, plugin: PluginForm) => {
  set(currentPluginAtom, plugin);
  set(currentDatasourceAtom, {
    ...INITIAL_DATASOURCE,
    pluginId: plugin.pluginId,
    datasourceConfiguration: {
      authenticationType: plugin.authType,
    },
  });
});

export const updateFormValueAtom = atom(
  null,
  (get, set, { path, value }: { path: string; value: unknown }) => {
    const currentDs = get(currentDatasourceAtom);
    const newDs = produce(currentDs, (draft) => {
      _.set(draft, path, value);
    });
    set(currentDatasourceAtom, newDs);
  }
);
