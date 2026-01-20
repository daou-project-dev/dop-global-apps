import { useAtom, useSetAtom } from 'jotai';
import { currentDatasourceAtom, updateFormValueAtom } from '../../store/atoms';
import type { ControlProps } from '../../store/types';
import _ from 'lodash';
import styles from './RadioButtonControl.module.css';

export function RadioButtonControl(props: ControlProps) {
  const { configProperty, label, options, hidden } = props;
  const updateFormValue = useSetAtom(updateFormValueAtom);
  const [datasource] = useAtom(currentDatasourceAtom);
  
  const value = _.get(datasource, configProperty, props.initialValue || '');

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
              name={configProperty} // Group by property name
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
