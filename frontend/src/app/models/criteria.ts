export interface Requirement {
  id: string;
  description: string;
  module: string;
  part: number;
}

export interface Criterion {
  id: string;
  title: string;
  question: string;
  requirements: Requirement[];
}

