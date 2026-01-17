import type { Page, Route } from '@playwright/test';

type Person = {
  id: string;
  firstName: string;
  lastName: string;
  topic: string;
  submissionDate: string;
};

type Requirement = {
  id: string;
  description: string;
  module: string;
  part: number;
};

type Criterion = {
  id: string;
  title: string;
  question: string;
  requirements: Requirement[];
};

type IpaDataset = {
  ipaName: string | null;
  topic: string | null;
  candidate: { fullName: string | null; firstName: string | null; lastName: string | null } | null;
  startDate: string | null;
  endDate: string | null;
  criteria: Criterion[];
};

type Progress = {
  id: string;
  personId: string;
  criterionId: string;
  checkedRequirements: string[];
  note: string | null;
};

type BackendState = {
  person: Person;
  criteria: Criterion[];
  ipaDataset: IpaDataset;
  progressByCriterionId: Record<string, Progress>;
};

const json = async (route: Route, payload: unknown, status = 200) => {
  return await route.fulfill({ status, contentType: 'application/json', body: JSON.stringify(payload) });
};

const calculateGrade = (checked: number, total: number): number => {
  if (total <= 0) return 0;
  if (checked >= total) return 3;
  if (checked >= Math.ceil((2 * total) / 3)) return 2;
  if (checked >= Math.ceil(total / 3)) return 1;
  return 0;
};

export const createDefaultBackendState = (): BackendState => {
  const person: Person = {
    id: 'p1',
    firstName: 'Max',
    lastName: 'Muster',
    topic: 'Notenrechner',
    submissionDate: '2026-01-17'
  };

  const criteria: Criterion[] = [
    {
      id: 'A01',
      title: 'Automatisierung',
      question: 'Ist die Pipeline automatisiert?',
      requirements: [
        { id: 'A01-1', description: 'CI baut automatisch', module: '324', part: 1 },
        { id: 'A01-2', description: 'Tests laufen automatisch', module: '450', part: 1 }
      ]
    }
  ];

  return {
    person,
    criteria,
    ipaDataset: {
      ipaName: 'QV BiVo 2021',
      topic: 'Notenrechner',
      candidate: { fullName: 'Max Muster', firstName: 'Max', lastName: 'Muster' },
      startDate: null,
      endDate: null,
      criteria
    },
    progressByCriterionId: {}
  };
};

export const mockBackend = async (page: Page, initialState: BackendState = createDefaultBackendState()): Promise<void> => {
  const state = initialState;
  const personId = state.person.id;

  await page.route('**/persons', async (route) => {
    const request = route.request();

    if (request.method() === 'GET') {
      return json(route, [state.person]);
    }

    if (request.method() === 'POST') {
      const body = request.postDataJSON() as Partial<Person>;
      const created: Person = {
        id: 'p2',
        firstName: (body.firstName ?? '').toString(),
        lastName: (body.lastName ?? '').toString(),
        topic: (body.topic ?? '').toString(),
        submissionDate: (body.submissionDate ?? '').toString()
      };
      state.person = created;
      return json(route, created, 201);
    }

    return route.fallback();
  });

  await page.route('**/criteria', async (route) => {
    if (route.request().method() !== 'GET') {
      return route.fallback();
    }
    return json(route, state.criteria);
  });

  await page.route(`**/ipa/${personId}`, async (route) => {
    if (route.request().method() !== 'GET') {
      return route.fallback();
    }
    return json(route, state.ipaDataset);
  });

  await page.route(`**/progress/${personId}`, async (route) => {
    const request = route.request();

    if (request.method() === 'GET') {
      return json(route, Object.values(state.progressByCriterionId));
    }

    if (request.method() === 'POST') {
      const body = request.postDataJSON() as { criterionId: string; checkedRequirements: string[]; note?: string | null; id?: string };
      const saved: Progress = {
        id: body.id ?? `${personId}-${body.criterionId}`,
        personId,
        criterionId: body.criterionId,
        checkedRequirements: body.checkedRequirements ?? [],
        note: body.note ?? null
      };
      state.progressByCriterionId[body.criterionId] = saved;
      return json(route, saved);
    }

    return route.fallback();
  });

  await page.route(`**/evaluation/${personId}`, async (route) => {
    if (route.request().method() !== 'GET') {
      return route.fallback();
    }

    const evaluations = state.ipaDataset.criteria.map((criterion) => {
      const progress = state.progressByCriterionId[criterion.id];
      const checked = progress?.checkedRequirements?.length ?? 0;
      const total = criterion.requirements.length;
      return {
        criterionId: criterion.id,
        totalRequirements: total,
        checkedRequirements: checked,
        grade: calculateGrade(checked, total)
      };
    });

    return json(route, evaluations);
  });

  await page.route(`**/results/${personId}`, async (route) => {
    if (route.request().method() !== 'GET') {
      return route.fallback();
    }

    const results = state.ipaDataset.criteria.map((criterion) => {
      const progress = state.progressByCriterionId[criterion.id];
      const checkedRequirements = progress?.checkedRequirements ?? [];
      const fulfilledCount = checkedRequirements.length;
      const totalCount = criterion.requirements.length;
      return {
        criterionId: criterion.id,
        fulfilledCount,
        totalCount,
        gradeLevel: calculateGrade(fulfilledCount, totalCount),
        checkedRequirements,
        note: progress?.note ?? null,
        title: criterion.title
      };
    });

    return json(route, { personId, results });
  });

  await page.route(`**/results/${personId}/*`, async (route) => {
    if (route.request().method() !== 'GET') {
      return route.fallback();
    }

    const url = route.request().url();
    const criterionId = url.split('/').pop() ?? '';
    const criterion = state.ipaDataset.criteria.find((item) => item.id === criterionId);
    if (!criterion) {
      return json(route, { message: 'not found' }, 404);
    }

    const progress = state.progressByCriterionId[criterionId];
    const checkedRequirements = progress?.checkedRequirements ?? [];
    const fulfilledCount = checkedRequirements.length;
    const totalCount = criterion.requirements.length;

    return json(route, {
      criterionId,
      fulfilledCount,
      totalCount,
      gradeLevel: calculateGrade(fulfilledCount, totalCount),
      checkedRequirements,
      note: progress?.note ?? null,
      title: criterion.title
    });
  });
};
