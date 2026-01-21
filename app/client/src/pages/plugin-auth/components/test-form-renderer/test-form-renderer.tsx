import type { PluginTestForm } from '../../../../store/types';

import { useTestForm } from './hooks/use-test-form';
import { TestTabList } from './components/test-tab-list';
import { TestControlRenderer } from './components/test-control-renderer';
import { TestResultPanel } from './components/test-result-panel';

import styles from './test-form-renderer.module.css';

interface TestFormRendererProps {
  testForm: PluginTestForm;
}

export function TestFormRenderer({ testForm }: TestFormRendererProps) {
  const {
    activeTab,
    activeTabId,
    setActiveTabId,
    inputs,
    setInputValue,
    result,
    isLoading,
    execute,
  } = useTestForm({ testForm });

  return (
    <div className={styles.container}>
      <h3 className={styles.title}>API 테스트</h3>

      <TestTabList
        tabs={testForm.tabs}
        activeTabId={activeTabId}
        onTabChange={setActiveTabId}
      />

      {activeTab.description && (
        <p className={styles.description}>{activeTab.description}</p>
      )}

      <div className={styles.controls}>
        {activeTab.controls.map((control) => (
          <TestControlRenderer
            key={control.name}
            control={control}
            value={inputs[control.name] ?? ''}
            onChange={(value) => setInputValue(control.name, value)}
          />
        ))}
      </div>

      <div className={styles.actions}>
        <button
          type="button"
          className={styles.button}
          onClick={execute}
          disabled={isLoading}
        >
          {isLoading ? '실행 중...' : '실행'}
        </button>
      </div>

      {result && <TestResultPanel result={result} />}
    </div>
  );
}
