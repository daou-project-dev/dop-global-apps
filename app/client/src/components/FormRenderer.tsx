import { useAtom } from 'jotai';
import { currentPluginAtom, currentDatasourceAtom } from '../store/atoms';
import type { ControlProps, ControlType } from '../store/types';
import { InputTextControl } from './controls/InputTextControl';
import { DropDownControl } from './controls/DropDownControl';
import { RadioButtonControl } from './controls/RadioButtonControl';
import styles from './FormRenderer.module.css';
import clsx from 'clsx';

// --- Factory Logic (FormControlFactory) ---

const ControlFactory: Record<ControlType, React.FC<ControlProps>> = {
  INPUT_TEXT: InputTextControl,
  DROP_DOWN: DropDownControl,
  CHECKBOX: () => <div>Checkbox Not Implemented</div>,
  RADIO_BUTTON: RadioButtonControl
};

// --- Renderer ---

export function FormRenderer() {
  const [currentPlugin] = useAtom(currentPluginAtom);
  const [currentDatasource] = useAtom(currentDatasourceAtom);
  
  // 1. Check Authentication Type first (Appsmith Logic)
  
  const isOAuth = currentPlugin.authType === 'oAuth2';
  
  const handleSave = () => {
    console.log('Saving Datasource:', currentDatasource);
    alert('Datasource Saved! Check console for JSON.');
  };

  const handleAuthorize = () => {
      alert('Redirecting to Google OAuth...');
  };

  return (
    <div>
      {/* 2. Render Form Fields from JSON */}
      {currentPlugin.formConfig.map((control) => {
          const Component = ControlFactory[control.controlType];
          if (!Component) return <div key={control.configProperty}>Unknown Control</div>;
          
          return <Component key={control.configProperty} {...control} />;
      })}
      
      {/* 3. Render Actions based on Auth Type */}
      <div className={styles.formFooter}>
          {isOAuth ? (
              <button 
                className={clsx(styles.button, styles.oauthButton)} 
                onClick={handleAuthorize}
              >
                  Save and Authorize
              </button>
          ) : (
              <button 
                className={styles.button} 
                onClick={handleSave}
              >
                  Save
              </button>
          )}
      </div>
    </div>
  );
}
