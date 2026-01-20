import { useAtom, useSetAtom } from 'jotai';
import { currentPluginAtom, currentDatasourceAtom, switchPluginAtom, S3_FORM, GOOGLE_SHEETS_FORM, GOOGLE_CALENDAR_FORM } from './store/atoms';
import { FormRenderer } from './components/FormRenderer';
import styles from './App.module.css';
import clsx from 'clsx';

export function App() {
  const [currentPlugin] = useAtom(currentPluginAtom);
  const [currentDatasource] = useAtom(currentDatasourceAtom);
  const switchPlugin = useSetAtom(switchPluginAtom);

  return (
    <div className={styles.appContainer}>
      <div className={styles.sidebar}>
        <h3>Plugins</h3>
        <button 
          className={clsx(styles.pluginButton, { [styles.pluginButtonActive]: currentPlugin.pluginId === S3_FORM.pluginId })}
          onClick={() => switchPlugin(S3_FORM)}
        >
          Amazon S3
        </button>
        <button 
          className={clsx(styles.pluginButton, { [styles.pluginButtonActive]: currentPlugin.pluginId === GOOGLE_SHEETS_FORM.pluginId })}
          onClick={() => switchPlugin(GOOGLE_SHEETS_FORM)}
        >
          Google Sheets (OAuth)
        </button>
        <button 
          className={clsx(styles.pluginButton, { [styles.pluginButtonActive]: currentPlugin.pluginId === GOOGLE_CALENDAR_FORM.pluginId })}
          onClick={() => switchPlugin(GOOGLE_CALENDAR_FORM)}
        >
          Google Calendar
        </button>
      </div>
      
      <div className={styles.mainContent}>
        <h1 className={styles.header}>Configuring: {currentPlugin.pluginName}</h1>
        
        {/* The Core Dynamic Form */}
        <div style={{ maxWidth: '500px' }}>
          <FormRenderer />
        </div>

        {/* Live Preview of the Redux/Zustand State */}
        <pre className={styles.jsonPreview}>
          {JSON.stringify(currentDatasource, null, 2)}
        </pre>
      </div>
    </div>
  );
}

export default App;
