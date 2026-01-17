export interface AppMetadata {
  ipaName: string | null;
  topic: string | null;
  candidateFullName: string | null;
  candidateFirstName: string | null;
  candidateLastName: string | null;
  activeDatasetId: string | null;
}

export interface IpaImportResponse {
  datasetId: string;
  personId: string;
  ipaName: string | null;
  topic: string | null;
  candidateFullName: string | null;
  criteriaCount: number;
}

