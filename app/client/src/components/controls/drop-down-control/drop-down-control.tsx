import { useAtom, useSetAtom } from 'jotai';
import _ from 'lodash';

import { currentDatasourceAtom, updateFormValueAtom } from '../../../store';

import type { DropDownControlProps } from './types';
import styles from './drop-down-control.module.css';

export function DropDownControl(props: DropDownControlProps) {
  const { configProperty, label, options, hidden } = props;
  const updateFormValue = useSetAtom(updateFormValueAtom);
  const [datasource] = useAtom(currentDatasourceAtom);

  const value = _.get(datasource, configProperty, '');

  const handleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    updateFormValue({ path: configProperty, value: e.target.value });
  };

  if (hidden) return null;

  return (
    <div className={styles.container}>
      <label className={styles.label}>{label}</label>
      <select className={styles.select} value={value} onChange={handleChange}>
        <option value="">Select an option</option>
        {options?.map((opt) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
    </div>
  );
}
