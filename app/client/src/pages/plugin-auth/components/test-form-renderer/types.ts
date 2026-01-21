/** 탭별 입력값 */
export type InputsMap = Record<string, Record<string, string>>;

/** 테스트 결과 */
export interface TestResult {
  success: boolean;
  timestamp: string;
  data: string;
  error?: string;
}

/** 탭별 결과 */
export type ResultsMap = Record<string, TestResult>;
