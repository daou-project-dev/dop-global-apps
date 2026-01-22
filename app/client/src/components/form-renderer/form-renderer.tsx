import { useAtom } from 'jotai';
import clsx from 'clsx';

import { currentPluginAtom } from '../../store';
import type { ControlProps, ControlType } from '../../store';
import {
  InputTextControl,
  DropDownControl,
  RadioButtonControl,
} from '../controls';

import styles from './form-renderer.module.css';

interface FormRendererProps {
  onSubmit?: () => void;
  isSubmitting?: boolean;
}

const ControlFactory: Record<ControlType, React.FC<ControlProps>> = {
  INPUT_TEXT: InputTextControl,
  DROP_DOWN: DropDownControl,
  CHECKBOX: () => <div>Checkbox Not Implemented</div>,
  RADIO_BUTTON: RadioButtonControl,
};

export function FormRenderer({ onSubmit, isSubmitting = false }: FormRendererProps) {
  const [currentPlugin] = useAtom(currentPluginAtom);

  const isOAuth = currentPlugin.authType === 'oAuth2';

  return (
    <div>
      {currentPlugin.formConfig.map((control) => {
        const Component = ControlFactory[control.controlType];
        if (!Component)
          return <div key={control.configProperty}>Unknown Control</div>;

        return <Component key={control.configProperty} {...control} />;
      })}

      <div className={styles.formFooter}>
        <button
          className={clsx(styles.button, isOAuth && styles.oauthButton)}
          onClick={onSubmit}
          disabled={isSubmitting}
        >
          {isSubmitting
            ? '저장 중...'
            : isOAuth
              ? 'Save and Authorize'
              : 'Save'}
        </button>
      </div>
    </div>
  );
}
