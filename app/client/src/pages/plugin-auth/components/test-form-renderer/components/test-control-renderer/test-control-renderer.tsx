import styles from './test-control-renderer.module.css';

import type { TestControlProps } from '../../../../../../store/types';

interface TestControlRendererProps {
  control: TestControlProps;
  value: string;
  onChange: (value: string) => void;
}

export function TestControlRenderer({ control, value, onChange }: TestControlRendererProps) {
  const { controlType, label, name, placeholder, required, options } = control;

  const renderControl = () => {
    switch (controlType) {
      case 'INPUT_TEXT':
        return (
          <input
            type="text"
            id={name}
            className={styles.input}
            placeholder={placeholder}
            value={value}
            onChange={(e) => onChange(e.target.value)}
          />
        );

      case 'TEXTAREA':
        return (
          <textarea
            id={name}
            className={styles.textarea}
            placeholder={placeholder}
            value={value}
            onChange={(e) => onChange(e.target.value)}
            rows={3}
          />
        );

      case 'DROP_DOWN':
        return (
          <select
            id={name}
            className={styles.select}
            value={value}
            onChange={(e) => onChange(e.target.value)}
          >
            <option value="">{placeholder ?? '선택하세요'}</option>
            {options?.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        );

      default:
        return (
          <input
            type="text"
            id={name}
            className={styles.input}
            placeholder={placeholder}
            value={value}
            onChange={(e) => onChange(e.target.value)}
          />
        );
    }
  };

  return (
    <div className={styles.field}>
      <label htmlFor={name} className={styles.label}>
        {label}
        {required && <span className={styles.required}>*</span>}
      </label>
      {renderControl()}
    </div>
  );
}
