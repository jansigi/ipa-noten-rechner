export type SubcriterionId = string;
export type CriterionId = string;
export type PartId = string;

export interface Subcriterion {
  id: SubcriterionId;
  description: string;
  points?: number;
}

export interface Criterion {
  id: CriterionId;
  code: string;
  title: string;
  partId: PartId;
  subcriteria: Subcriterion[];
}

export interface Part {
  id: PartId;
  name: string;
  weight?: number;
  criteriaIds: CriterionId[];
  locked?: boolean;
  badge?: string;
}

export interface EvaluationDataset {
  parts: Part[];
  criteria: Criterion[];
}

export const defaultEvaluationDataset: EvaluationDataset = {
  parts: [
    { id: 'teil-1', name: 'Teil 1', weight: 1, criteriaIds: ['c1', 'c2'] },
    { id: 'teil-2', name: 'Teil 2', weight: 1, criteriaIds: ['c3', 'c4'] }
  ],
  criteria: [
    {
      id: 'c1',
      code: 'C1',
      partId: 'teil-1',
      title: 'Grobentwurf & Datenmodell',
      subcriteria: [
        { id: 'c1-s1', description: 'Zieldefinition und Scope klar dokumentiert', points: 1 },
        { id: 'c1-s2', description: 'Domänenmodell mit Beziehungen skizziert', points: 1 },
        { id: 'c1-s3', description: 'Risiken/Annahmen früh festgehalten', points: 1 }
      ]
    },
    {
      id: 'c2',
      code: 'C2',
      partId: 'teil-1',
      title: 'Architektur & Schnittstellen',
      subcriteria: [
        { id: 'c2-s1', description: 'Schnittstellen und Verträge dokumentiert', points: 1 },
        { id: 'c2-s2', description: 'Fehler- und Testfälle definiert', points: 1 },
        { id: 'c2-s3', description: 'Technologieentscheid begründet', points: 1 }
      ]
    },
    {
      id: 'c3',
      code: 'C3',
      partId: 'teil-2',
      title: 'Testdurchführung',
      subcriteria: [
        { id: 'c3-s1', description: 'Testfälle umgesetzt und nachvollziehbar', points: 1 },
        { id: 'c3-s2', description: 'Automatisierung wo sinnvoll', points: 1 },
        { id: 'c3-s3', description: 'Ergebnisse ausgewertet und dokumentiert', points: 1 }
      ]
    },
    {
      id: 'c4',
      code: 'C4',
      partId: 'teil-2',
      title: 'Reflexion & Qualität',
      subcriteria: [
        { id: 'c4-s1', description: 'Abweichungen begründet', points: 1 },
        { id: 'c4-s2', description: 'Lessons Learned festgehalten', points: 1 },
        { id: 'c4-s3', description: 'Qualitätssicherung bewertet', points: 1 }
      ]
    }
  ]
};
