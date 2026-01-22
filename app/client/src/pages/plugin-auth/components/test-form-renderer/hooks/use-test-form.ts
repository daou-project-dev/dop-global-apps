import { useState, useCallback, useMemo } from 'react';

import { slackApi } from '../../../api';

import type { PluginTestForm, TestTab } from '../../../../../store/types';
import type { InputsMap, ResultsMap, TestResult } from '../types';

interface UseTestFormOptions {
  testForm: PluginTestForm;
  externalId: string;
}

interface UseTestFormReturn {
  activeTab: TestTab;
  activeTabId: string;
  setActiveTabId: (tabId: string) => void;
  inputs: Record<string, string>;
  setInputValue: (name: string, value: string) => void;
  result: TestResult | null;
  isLoading: boolean;
  execute: () => Promise<void>;
  validateInputs: () => string | null;
}

export function useTestForm({ testForm, externalId }: UseTestFormOptions): UseTestFormReturn {
  const [activeTabId, setActiveTabId] = useState(testForm.tabs[0]?.tabId ?? '');
  const [inputsMap, setInputsMap] = useState<InputsMap>(() => {
    const initial: InputsMap = {};
    testForm.tabs.forEach((tab) => {
      initial[tab.tabId] = {};
      tab.controls.forEach((control) => {
        initial[tab.tabId][control.name] = control.initialValue ?? '';
      });
    });
    return initial;
  });
  const [resultsMap, setResultsMap] = useState<ResultsMap>({});
  const [isLoading, setIsLoading] = useState(false);

  const activeTab = useMemo(
    () => testForm.tabs.find((tab) => tab.tabId === activeTabId) ?? testForm.tabs[0],
    [testForm.tabs, activeTabId]
  );

  const inputs = inputsMap[activeTabId] ?? {};
  const result = resultsMap[activeTabId] ?? null;

  const setInputValue = useCallback(
    (name: string, value: string) => {
      setInputsMap((prev) => ({
        ...prev,
        [activeTabId]: {
          ...prev[activeTabId],
          [name]: value,
        },
      }));
    },
    [activeTabId]
  );

  const validateInputs = useCallback((): string | null => {
    for (const control of activeTab.controls) {
      if (control.required && !inputs[control.name]?.trim()) {
        return `${control.label}을(를) 입력하세요`;
      }
    }
    return null;
  }, [activeTab.controls, inputs]);

  const execute = useCallback(async () => {
    const validationError = validateInputs();
    if (validationError) {
      setResultsMap((prev) => ({
        ...prev,
        [activeTabId]: {
          success: false,
          timestamp: new Date().toLocaleTimeString(),
          data: '',
          error: validationError,
        },
      }));
      return;
    }

    setIsLoading(true);

    try {
      const { uri } = activeTab.api;

      // inputs를 params로 변환 (빈 문자열 제외)
      const params: Record<string, unknown> = {
        externalId,
      };
      Object.entries(inputs).forEach(([key, value]) => {
        if (value !== '') {
          params[key] = value;
        }
      });

      const response = await slackApi.execute({
        pluginId: testForm.pluginId,
        action: uri,
        params,
      });

      const { success, body: responseBody, error } = response.data;

      let formattedData = responseBody;
      try {
        formattedData = JSON.stringify(JSON.parse(responseBody), null, 2);
      } catch {
        // JSON 파싱 실패 시 원본 유지
      }

      setResultsMap((prev) => ({
        ...prev,
        [activeTabId]: {
          success,
          timestamp: new Date().toLocaleTimeString(),
          data: formattedData,
          error: error ?? undefined,
        },
      }));
    } catch (err) {
      setResultsMap((prev) => ({
        ...prev,
        [activeTabId]: {
          success: false,
          timestamp: new Date().toLocaleTimeString(),
          data: '',
          error: err instanceof Error ? err.message : '요청 실패',
        },
      }));
    } finally {
      setIsLoading(false);
    }
  }, [activeTab, activeTabId, inputs, validateInputs, externalId, testForm.pluginId]);

  return {
    activeTab,
    activeTabId,
    setActiveTabId,
    inputs,
    setInputValue,
    result,
    isLoading,
    execute,
    validateInputs,
  };
}
