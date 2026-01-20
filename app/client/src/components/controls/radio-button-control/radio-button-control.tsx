import { useAtom, useSetAtom } from 'jotai';
import _ from 'lodash';

import { currentDatasourceAtom, updateFormValueAtom } from '../../../store';

import type { RadioButtonControlProps } from './types';
import styles from './radio-button-control.module.css';

export function RadioButtonControl(props: RadioButtonControlProps) {
  const { configProperty, label, options, hidden, initialValue } = props;
  const updateFormValue = useSetAtom(updateFormValueAtom);
  const [datasource] = useAtom(currentDatasourceAtom);

  const value = _.get(datasource, configProperty, initialValue || '');

  const handleChange = (val: string) => {
    updateFormValue({ path: configProperty, value: val });
  };

  if (hidden) return null;

  return (
    <div className={styles.container}>
      <label className={styles.label}>{label}</label>
      <div className={styles.radioGroup}>
        {options?.map((opt) => (
          <label key={opt.value} className={styles.radioLabel}>
            <input
              type="radio"
              name={configProperty}
              value={opt.value}
              checked={value === opt.value}
              onChange={() => handleChange(opt.value)}
              className={styles.radioInput}
            />
            {opt.label}
          </label>
        ))}
      </div>
    </div>
  );
}
