export interface CriterionResult {
  criterionId: string;
  fulfilledCount: number;
  totalCount: number;
  gradeLevel: number;
  checkedRequirements: string[];
  note: string | null;
  title: string;
}

export interface PersonResults {
  personId: string;
  results: CriterionResult[];
}
