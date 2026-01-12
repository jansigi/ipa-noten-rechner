export interface ActivitySnapshot {
  date: string;
  speed: number;
  throughput: number;
  quality: number;
  focus: number;
}

export interface ActivityPhase {
  id: string;
  name: string;
  goal: string;
  completionRatio: number;
  status: 'completed' | 'in-progress' | 'upcoming';
}

export interface ActivityGoals {
  speed: number;
  throughput: number;
  quality: number;
  focus: number;
}

export interface Activity {
  id: string;
  name: string;
  category: string;
  owner: string;
  description: string;
  baselineSpeed: number;
  targetSpeed: number;
  goals: ActivityGoals;
  snapshots: ActivitySnapshot[];
  phases: ActivityPhase[];
  achievements: string[];
  updatedAt: string;
}

export interface ActivityDataset {
  activities: Activity[];
  generatedAt: string;
}

export const demoActivityDataset: ActivityDataset = {
  generatedAt: '2026-01-12T00:00:00Z',
  activities: [
    {
      id: 'act-architecture',
      name: 'Architektur-Blueprint erstellen',
      category: 'Planung',
      owner: 'Solution Team Nord',
      description:
        'Erarbeitung des technischen Zielbilds inklusive Schnittstellenkonzept, Sicherheitsarchitektur sowie Priorisierung der Umsetzungsschritte.',
      baselineSpeed: 5.4,
      targetSpeed: 9.5,
      goals: {
        speed: 9.5,
        throughput: 18,
        quality: 90,
        focus: 85
      },
      snapshots: [
        { date: '2025-12-02', speed: 5.4, throughput: 11, quality: 72, focus: 66 },
        { date: '2025-12-09', speed: 6.1, throughput: 12, quality: 75, focus: 68 },
        { date: '2025-12-16', speed: 6.9, throughput: 13, quality: 78, focus: 70 },
        { date: '2025-12-23', speed: 7.6, throughput: 15, quality: 81, focus: 74 },
        { date: '2025-12-30', speed: 8.4, throughput: 16, quality: 84, focus: 79 },
        { date: '2026-01-06', speed: 9.0, throughput: 17, quality: 87, focus: 82 },
        { date: '2026-01-12', speed: 9.3, throughput: 18, quality: 89, focus: 84 }
      ],
      phases: [
        {
          id: 'phase-discovery',
          name: 'Discovery & Kontext',
          goal: 'Stakeholderziele und Randbedingungen aufnehmen',
          completionRatio: 1,
          status: 'completed'
        },
        {
          id: 'phase-design',
          name: 'Lösungsdesign',
          goal: 'Komponenten- und Schnittstellenmodell finale Abstimmung',
          completionRatio: 0.78,
          status: 'in-progress'
        },
        {
          id: 'phase-review',
          name: 'Review & Freigabe',
          goal: 'Architekturentscheid dokumentiert und genehmigt',
          completionRatio: 0.32,
          status: 'upcoming'
        }
      ],
      achievements: [
        'CI/CD-Pipeline für Architektur-Docs automatisiert',
        'Security-Guidelines in allen Modulen verankert',
        'Stakeholder-Feedback-Runden verkürzt'
      ],
      updatedAt: '2026-01-12T07:15:00Z'
    },
    {
      id: 'act-implementation',
      name: 'Implementierung Service Layer',
      category: 'Umsetzung',
      owner: 'Feature Squad Delta',
      description:
        'Implementierung der Kern-Services inklusive Datenbankanbindung, Fokus auf Durchsatz und Stabilität.',
      baselineSpeed: 8.1,
      targetSpeed: 12.5,
      goals: {
        speed: 12.5,
        throughput: 25,
        quality: 92,
        focus: 87
      },
      snapshots: [
        { date: '2025-12-05', speed: 8.1, throughput: 16, quality: 78, focus: 72 },
        { date: '2025-12-12', speed: 8.8, throughput: 17, quality: 80, focus: 73 },
        { date: '2025-12-19', speed: 9.6, throughput: 19, quality: 82, focus: 74 },
        { date: '2025-12-26', speed: 10.5, throughput: 21, quality: 84, focus: 76 },
        { date: '2026-01-02', speed: 11.3, throughput: 22, quality: 86, focus: 79 },
        { date: '2026-01-09', speed: 12.0, throughput: 24, quality: 88, focus: 82 },
        { date: '2026-01-12', speed: 12.4, throughput: 25, quality: 90, focus: 85 }
      ],
      phases: [
        {
          id: 'phase-backlog',
          name: 'Backlog Ready',
          goal: 'User Stories verfeinert, Abhängigkeiten aufgelöst',
          completionRatio: 1,
          status: 'completed'
        },
        {
          id: 'phase-sprint',
          name: 'Laufende Sprints',
          goal: 'Durchschnittliche Teamleistung stabilisieren',
          completionRatio: 0.86,
          status: 'in-progress'
        },
        {
          id: 'phase-hardening',
          name: 'Hardening',
          goal: 'Performance- und Resilienztests finalisieren',
          completionRatio: 0.48,
          status: 'in-progress'
        }
      ],
      achievements: [
        'Fehlerrate nach Deploy um 35% reduziert',
        'Durchschnittliche Lead Time um 1.4 Tage gesenkt',
        'Observability-Dashboard produktiv gesetzt'
      ],
      updatedAt: '2026-01-12T09:42:00Z'
    },
    {
      id: 'act-quality',
      name: 'Qualitätssicherung automatisieren',
      category: 'Qualität',
      owner: 'QA Guild West',
      description:
        'Aufbau eines automatisierten QA-Frameworks mit Fokus auf Stabilität, Testabdeckung und Feedback-Zyklen.',
      baselineSpeed: 4.8,
      targetSpeed: 8.0,
      goals: {
        speed: 8,
        throughput: 14,
        quality: 95,
        focus: 90
      },
      snapshots: [
        { date: '2025-12-03', speed: 4.8, throughput: 8, quality: 82, focus: 78 },
        { date: '2025-12-10', speed: 5.1, throughput: 9, quality: 84, focus: 79 },
        { date: '2025-12-17', speed: 5.7, throughput: 10, quality: 86, focus: 81 },
        { date: '2025-12-24', speed: 6.2, throughput: 11, quality: 88, focus: 83 },
        { date: '2025-12-31', speed: 6.9, throughput: 12, quality: 90, focus: 85 },
        { date: '2026-01-07', speed: 7.3, throughput: 13, quality: 92, focus: 87 },
        { date: '2026-01-12', speed: 7.7, throughput: 13, quality: 93, focus: 89 }
      ],
      phases: [
        {
          id: 'phase-assessment',
          name: 'Ist-Analyse',
          goal: 'Abdeckung bestehender Test-Suites bewerten',
          completionRatio: 1,
          status: 'completed'
        },
        {
          id: 'phase-automation',
          name: 'Automatisierung',
          goal: 'Smoke- & Regressionstests automatisieren',
          completionRatio: 0.74,
          status: 'in-progress'
        },
        {
          id: 'phase-observability',
          name: 'Observability',
          goal: 'Qualitätsmetriken in Reporting integrieren',
          completionRatio: 0.41,
          status: 'upcoming'
        }
      ],
      achievements: [
        'Testabdeckung in kritischen Bereichen auf 78% erhöht',
        'Build-Feedback von 18 auf 9 Minuten reduziert',
        'Rollbacks im Dezember vollständig vermieden'
      ],
      updatedAt: '2026-01-12T06:58:00Z'
    }
  ]
};


