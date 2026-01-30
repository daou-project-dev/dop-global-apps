import { atom } from 'jotai';

import type { PluginForm } from '../types';

const EMPTY_PLUGIN: PluginForm = {
  pluginId: '',
  pluginName: '',
  authType: '',
  formConfig: [],
  authConfig: undefined,
};

export const currentPluginAtom = atom<PluginForm>(EMPTY_PLUGIN);
