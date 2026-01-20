import { useAtom, useSetAtom } from 'jotai';
import { currentDatasourceAtom, updateFormValueAtom } from '../../store/atoms';
import type { ControlProps } from '../../store/types';
import _ from 'lodash';
import styles from './DropDownControl.module.css';

export function DropDownControl(props: ControlProps) {
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
