export interface CriterionProgress {
  id: string;
  personId: string;
  criterionId: string;
  checkedRequirements: string[];
  note: string | null;
}

export interface CriterionProgressRequest {
  id?: string;
  criterionId: string;
  checkedRequirements: string[];
  note?: string | null;
}
