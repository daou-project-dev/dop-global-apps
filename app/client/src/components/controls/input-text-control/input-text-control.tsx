import { useAtom, useSetAtom } from 'jotai';
import _ from 'lodash';

import { currentDatasourceAtom, updateFormValueAtom } from '../../../store';

import type { InputTextControlProps } from './types';
import styles from './input-text-control.module.css';

export function InputTextControl(props: InputTextControlProps) {
  const { configProperty, label, dataType, hidden } = props;
  const updateFormValue = useSetAtom(updateFormValueAtom);
  const [datasource] = useAtom(currentDatasourceAtom);

  const value = _.get(datasource, configProperty, '');

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    updateFormValue({ path: configProperty, value: e.target.value });
  };

  if (hidden) return null;

  return (
    <div className={styles.container}>
      <label className={styles.label}>{label}</label>
      <input
        className={styles.input}
        type={dataType === 'PASSWORD' ? 'password' : 'text'}
        value={value}
        onChange={handleChange}
      />
    </div>
  );
}
