import type { ControlProps, ControlType } from '../../store';

export type ControlFactory = Record<ControlType, React.FC<ControlProps>>;
