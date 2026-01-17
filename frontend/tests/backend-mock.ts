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
  persons: Person[];
  criteria: Criterion[];
  ipaDatasetByPersonId: Record<string, IpaDataset>;
  progressByPersonId: Record<string, Record<string, Progress>>;
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

  const dataset: IpaDataset = {
    ipaName: 'QV BiVo 2021',
    topic: person.topic,
    candidate: { fullName: `${person.firstName} ${person.lastName}`, firstName: person.firstName, lastName: person.lastName },
    startDate: null,
    endDate: null,
    criteria
  };

  return {
    persons: [person],
    criteria,
    ipaDatasetByPersonId: {
      [person.id]: dataset
    },
    progressByPersonId: {
      [person.id]: {}
    }
  };
};

export const mockBackend = async (
  page: Page,
  initialState: BackendState = createDefaultBackendState()
): Promise<void> => {
  const state = initialState;

  const getPersonIdFromUrl = (url: string): string => {
    const clean = url.split('?')[0];
    const parts = clean.split('/').filter(Boolean);
    return parts[parts.length - 1] ?? '';
  };

  const getPersonIdAndCriterionIdFromUrl = (url: string): { personId: string; criterionId: string } => {
    const clean = url.split('?')[0];
    const parts = clean.split('/').filter(Boolean);
    return {
      criterionId: parts[parts.length - 1] ?? '',
      personId: parts[parts.length - 2] ?? ''
    };
  };

  const ensurePersonData = (person: Person): void => {
    if (!state.ipaDatasetByPersonId[person.id]) {
      state.ipaDatasetByPersonId[person.id] = {
        ipaName: state.ipaDatasetByPersonId[state.persons[0].id]?.ipaName ?? 'QV BiVo 2021',
        topic: person.topic,
        candidate: {
          fullName: `${person.firstName} ${person.lastName}`,
          firstName: person.firstName,
          lastName: person.lastName
        },
        startDate: null,
        endDate: null,
        criteria: state.criteria
      };
    }

    state.progressByPersonId[person.id] ??= {};
  };

  await page.route('**/persons', async (route) => {
    const request = route.request();

    if (request.method() === 'GET') {
      return json(route, state.persons);
    }

    if (request.method() === 'POST') {
      const body = request.postDataJSON() as Partial<Person>;
      const created: Person = {
        id: `p${state.persons.length + 1}`,
        firstName: (body.firstName ?? '').toString(),
        lastName: (body.lastName ?? '').toString(),
        topic: (body.topic ?? '').toString(),
        submissionDate: (body.submissionDate ?? '').toString()
      };

      state.persons = [created, ...state.persons];
      ensurePersonData(created);

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

  // More specific route first: /results/{personId}/{criterionId}
  await page.route('**/results/*/*', async (route) => {
    if (route.request().method() !== 'GET') {
      return route.fallback();
    }

    const { personId, criterionId } = getPersonIdAndCriterionIdFromUrl(route.request().url());
    const dataset = state.ipaDatasetByPersonId[personId];
    if (!dataset) {
      return json(route, { message: 'not found' }, 404);
    }

    const criterion = dataset.criteria.find((item) => item.id === criterionId);
    if (!criterion) {
      return json(route, { message: 'not found' }, 404);
    }

    const progressMap = state.progressByPersonId[personId] ?? {};
    const progress = progressMap[criterionId];
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

  await page.route('**/results/*', async (route) => {
    if (route.request().method() !== 'GET') {
      return route.fallback();
    }

    const personId = getPersonIdFromUrl(route.request().url());
    const dataset = state.ipaDatasetByPersonId[personId];
    if (!dataset) {
      return json(route, { message: 'not found' }, 404);
    }

    const progressMap = state.progressByPersonId[personId] ?? {};

    const results = dataset.criteria.map((criterion) => {
      const progress = progressMap[criterion.id];
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

  await page.route('**/evaluation/*', async (route) => {
    if (route.request().method() !== 'GET') {
      return route.fallback();
    }

    const personId = getPersonIdFromUrl(route.request().url());
    const dataset = state.ipaDatasetByPersonId[personId];
    if (!dataset) {
      return json(route, { message: 'not found' }, 404);
    }

    const progressMap = state.progressByPersonId[personId] ?? {};

    const evaluations = dataset.criteria.map((criterion) => {
      const progress = progressMap[criterion.id];
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

  await page.route('**/ipa/*', async (route) => {
    if (route.request().method() !== 'GET') {
      return route.fallback();
    }

    const personId = getPersonIdFromUrl(route.request().url());
    const dataset = state.ipaDatasetByPersonId[personId];
    if (!dataset) {
      return json(route, { message: 'not found' }, 404);
    }

    return json(route, dataset);
  });

  await page.route('**/progress/*', async (route) => {
    const request = route.request();
    const personId = getPersonIdFromUrl(request.url());

    state.progressByPersonId[personId] ??= {};
    const progressMap = state.progressByPersonId[personId];

    if (request.method() === 'GET') {
      return json(route, Object.values(progressMap));
    }

    if (request.method() === 'POST') {
      const body = request.postDataJSON() as {
        criterionId: string;
        checkedRequirements: string[];
        note?: string | null;
        id?: string;
      };

      const saved: Progress = {
        id: body.id ?? `${personId}-${body.criterionId}`,
        personId,
        criterionId: body.criterionId,
        checkedRequirements: body.checkedRequirements ?? [],
        note: body.note ?? null
      };

      progressMap[body.criterionId] = saved;
      return json(route, saved);
    }

    return route.fallback();
  });

  // Ensure initial data exists for the default person
  for (const person of state.persons) {
    ensurePersonData(person);
  }
};
