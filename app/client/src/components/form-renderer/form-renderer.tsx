import clsx from 'clsx';
import { useAtom } from 'jotai';

import { currentPluginAtom } from '../../store';
import { InputTextControl, DropDownControl, RadioButtonControl } from '../controls';

import styles from './form-renderer.module.css';

import type { ControlProps, ControlType } from '../../store';

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
  const isServiceAccount = currentPlugin.authType === 'serviceAccount';

  const getButtonText = () => {
    if (isSubmitting) return '처리 중...';
    if (isOAuth) return 'Save and Authorize';
    if (isServiceAccount) return '인증';
    return 'Save';
  };

  return (
    <div>
      {currentPlugin.formConfig.map((control) => {
        const Component = ControlFactory[control.controlType];
        if (!Component) return <div key={control.configProperty}>Unknown Control</div>;

        return <Component key={control.configProperty} {...control} />;
      })}

      <div className={styles.formFooter}>
        <button
          className={clsx(styles.button, (isOAuth || isServiceAccount) && styles.oauthButton)}
          onClick={onSubmit}
          disabled={isSubmitting}
        >
          {getButtonText()}
        </button>
      </div>
    </div>
  );
}
