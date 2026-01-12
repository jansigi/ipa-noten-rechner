import { Criterion } from './criteria';

export interface Candidate {
  fullName: string | null;
  firstName: string | null;
  lastName: string | null;
}

export interface IpaDataset {
  ipaName: string | null;
  topic: string | null;
  candidate: Candidate | null;
  criteria: Criterion[];
}

