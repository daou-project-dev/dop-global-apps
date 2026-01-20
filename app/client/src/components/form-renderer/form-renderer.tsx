import { useAtom } from 'jotai';
import clsx from 'clsx';

import { currentPluginAtom, currentDatasourceAtom } from '../../store';
import type { ControlProps, ControlType } from '../../store';
import {
  InputTextControl,
  DropDownControl,
  RadioButtonControl,
} from '../controls';

import styles from './form-renderer.module.css';

const ControlFactory: Record<ControlType, React.FC<ControlProps>> = {
  INPUT_TEXT: InputTextControl,
  DROP_DOWN: DropDownControl,
  CHECKBOX: () => <div>Checkbox Not Implemented</div>,
  RADIO_BUTTON: RadioButtonControl,
};

export function FormRenderer() {
  const [currentPlugin] = useAtom(currentPluginAtom);
  const [currentDatasource] = useAtom(currentDatasourceAtom);

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
      {currentPlugin.formConfig.map((control) => {
        const Component = ControlFactory[control.controlType];
        if (!Component)
          return <div key={control.configProperty}>Unknown Control</div>;

        return <Component key={control.configProperty} {...control} />;
      })}

      <div className={styles.formFooter}>
        {isOAuth ? (
          <button
            className={clsx(styles.button, styles.oauthButton)}
            onClick={handleAuthorize}
          >
            Save and Authorize
          </button>
        ) : (
          <button className={styles.button} onClick={handleSave}>
            Save
          </button>
        )}
      </div>
    </div>
  );
}
