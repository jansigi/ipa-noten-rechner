export interface Person {
  id: string;
  firstName: string;
  lastName: string;
  topic: string;
  submissionDate: string;
}

export interface CreatePersonRequest {
  firstName: string;
  lastName: string;
  topic: string;
  submissionDate: string;
}

