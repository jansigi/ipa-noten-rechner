import { inject, Injectable, computed, signal } from '@angular/core';
import { BackendApiService } from './backend-api.service';
import { Person, CreatePersonRequest } from '../models/person';
import { Criterion } from '../models/criteria';
import { CriterionProgress, CriterionProgressRequest } from '../models/progress';
import { EvaluatedCriterion } from '../models/evaluation-result';
import { CriterionResult, PersonResults } from '../models/results';
import { firstValueFrom } from 'rxjs';
import { IpaDataset } from '../models/ipa';

type ProgressMap = Record<string, CriterionProgress>;

const emptyProgressPayload = (criterionId: string): CriterionProgressRequest => ({
  criterionId,
  checkedRequirements: [],
  note: null
});

@Injectable({ providedIn: 'root' })
export class EvaluationStoreService {
  private readonly api = inject(BackendApiService);

  readonly persons = signal<Person[]>([]);
  readonly personsLoading = signal(false);

  // Full catalog (Kriterien tab)
  readonly criteria = signal<Criterion[]>([]);
  readonly criteriaLoading = signal(false);

  // Per-person imported dataset (Checkliste tab)
  readonly personIpaDataset = signal<IpaDataset | null>(null);
  readonly personIpaDatasetLoading = signal(false);

  readonly selectedPersonId = signal<string | null>(null);
  readonly progress = signal<ProgressMap>({});
  readonly evaluation = signal<EvaluatedCriterion[]>([]);
  readonly results = signal<PersonResults | null>(null);

  readonly personDataLoading = signal(false);
  readonly error = signal<string | null>(null);

  readonly selectedPerson = computed<Person | null>(() => {
    const id = this.selectedPersonId();
    return this.persons().find((p) => p.id === id) ?? null;
  });

  readonly checklistCriteria = computed<Criterion[]>(() => this.personIpaDataset()?.criteria ?? []);

  readonly selectedIpaName = computed(() => this.personIpaDataset()?.ipaName ?? null);
  readonly selectedTopic = computed(() => this.personIpaDataset()?.topic ?? null);
  readonly selectedCandidateName = computed(() => {
    const candidate = this.personIpaDataset()?.candidate;
    if (!candidate) {
      return '';
    }
    return candidate.fullName?.trim() || [candidate.firstName, candidate.lastName].filter(Boolean).join(' ').trim();
  });

  readonly overallCompletionRatio = computed(() => {
    const current = this.results();
    if (!current || !current.results.length) {
      return 0;
    }
    const totals = current.results.reduce(
      (acc, item) => {
        acc.fulfilled += item.fulfilledCount;
        acc.total += item.totalCount;
        return acc;
      },
      { fulfilled: 0, total: 0 }
    );
    return totals.total === 0 ? 0 : totals.fulfilled / totals.total;
  });

  readonly averageGradeLevel = computed(() => {
    const current = this.results();
    if (!current || !current.results.length) {
      return 0;
    }
    const sum = current.results.reduce((acc, item) => acc + item.gradeLevel, 0);
    return sum / current.results.length;
  });

  constructor() {
    void this.loadCriteria();
    void this.loadPersons();
  }

  async loadCriteria(): Promise<void> {
    this.criteriaLoading.set(true);
    try {
      const data = await firstValueFrom(this.api.getCriteria());
      this.criteria.set(data);
      this.error.set(null);
    } catch {
      this.error.set('Kriterien konnten nicht geladen werden.');
    } finally {
      this.criteriaLoading.set(false);
    }
  }

  async loadPersons(): Promise<void> {
    this.personsLoading.set(true);
    try {
      const data = await firstValueFrom(this.api.getPersons());
      this.persons.set(data);

      if (!this.selectedPersonId() && data.length) {
        this.selectPerson(data[0].id);
      }

      this.error.set(null);
    } catch {
      this.error.set('Personen konnten nicht geladen werden.');
    } finally {
      this.personsLoading.set(false);
    }
  }

  async createPerson(payload: CreatePersonRequest): Promise<void> {
    try {
      const created = await firstValueFrom(this.api.createPerson(payload));
      this.persons.update((current) => [created, ...current]);
      this.selectPerson(created.id);
      this.error.set(null);
    } catch {
      this.error.set('Person konnte nicht erstellt werden.');
    }
  }

  async importIpaPdf(file: File): Promise<void> {
    this.personIpaDatasetLoading.set(true);
    try {
      const response = await firstValueFrom(this.api.uploadIpaPdf(file));
      await this.loadPersons();
      this.selectPerson(response.personId);
      this.error.set(null);
    } catch {
      this.error.set('Die Kriterien konnten nicht importiert werden.');
    } finally {
      this.personIpaDatasetLoading.set(false);
    }
  }

  selectPerson(personId: string): void {
    if (!personId || this.selectedPersonId() === personId) {
      return;
    }

    this.selectedPersonId.set(personId);

    // Reset per-person state while switching
    this.personIpaDataset.set(null);
    this.progress.set({});
    this.evaluation.set([]);
    this.results.set(null);

    void this.loadPersonIpaDataset(personId);
    void this.refreshPersonData();
  }

  async loadPersonIpaDataset(personId: string): Promise<void> {
    this.personIpaDatasetLoading.set(true);
    try {
      const dataset = await firstValueFrom(this.api.getIpaDataset(personId));
      this.personIpaDataset.set(dataset);
    } catch {
      this.personIpaDataset.set(null);
    } finally {
      this.personIpaDatasetLoading.set(false);
    }
  }

  async refreshPersonData(): Promise<void> {
    const personId = this.selectedPersonId();
    if (!personId) {
      return;
    }
    this.personDataLoading.set(true);
    try {
      const [progress, evaluation, results] = await Promise.all([
        firstValueFrom(this.api.getProgress(personId)),
        firstValueFrom(this.api.getEvaluation(personId)),
        firstValueFrom(this.api.getResults(personId))
      ]);
      const progressMap = progress.reduce<ProgressMap>((acc, item) => {
        acc[item.criterionId] = item;
        return acc;
      }, {});
      this.progress.set(progressMap);
      this.evaluation.set(evaluation);
      this.results.set(results);
      this.error.set(null);
    } catch {
      this.error.set('Daten der Person konnten nicht geladen werden.');
    } finally {
      this.personDataLoading.set(false);
    }
  }

  isRequirementChecked(criterionId: string, requirementId: string): boolean {
    const current = this.progress()[criterionId];
    return current?.checkedRequirements.includes(requirementId) ?? false;
  }

  noteFor(criterionId: string): string {
    return this.progress()[criterionId]?.note ?? '';
  }

  evaluationFor(criterionId: string): EvaluatedCriterion | undefined {
    return this.evaluation().find((item) => item.criterionId === criterionId);
  }

  resultFor(criterionId: string): CriterionResult | undefined {
    return this.results()?.results.find((item) => item.criterionId === criterionId);
  }

  async toggleRequirement(criterionId: string, requirementId: string): Promise<void> {
    const personId = this.selectedPersonId();
    if (!personId) {
      return;
    }
    const current = this.progress()[criterionId];
    const nextChecked = new Set(current?.checkedRequirements ?? []);
    if (nextChecked.has(requirementId)) {
      nextChecked.delete(requirementId);
    } else {
      nextChecked.add(requirementId);
    }
    await this.persistProgress(criterionId, {
      id: current?.id,
      criterionId,
      checkedRequirements: Array.from(nextChecked),
      note: current?.note ?? null
    });
  }

  async updateNote(criterionId: string, note: string): Promise<void> {
    const current = this.progress()[criterionId];
    await this.persistProgress(criterionId, {
      id: current?.id,
      criterionId,
      checkedRequirements: current?.checkedRequirements ?? [],
      note
    });
  }

  private async persistProgress(criterionId: string, payload: CriterionProgressRequest): Promise<void> {
    const personId = this.selectedPersonId();
    if (!personId) {
      return;
    }
    try {
      const saved = await firstValueFrom(this.api.saveProgress(personId, payload));
      this.progress.update((current) => ({ ...current, [criterionId]: saved }));
      await this.refreshEvaluationForCriterion(criterionId);
      await this.refreshResultForCriterion(criterionId);
      this.error.set(null);
    } catch {
      this.error.set('Fortschritt konnte nicht gespeichert werden.');
    }
  }

  private async refreshEvaluationForCriterion(criterionId: string): Promise<void> {
    const personId = this.selectedPersonId();
    if (!personId) {
      return;
    }
    try {
      const latest = await firstValueFrom(this.api.getEvaluation(personId));
      this.evaluation.set(latest);
    } catch {
      // keep previous evaluation if refresh fails
    }
  }

  private async refreshResultForCriterion(criterionId: string): Promise<void> {
    const personId = this.selectedPersonId();
    if (!personId) {
      return;
    }
    try {
      const latest = await firstValueFrom(this.api.getCriterionResult(personId, criterionId));
      this.results.update((current) => {
        if (!current) {
          return { personId, results: [latest] };
        }
        const nextResults = current.results.some((item) => item.criterionId === criterionId)
          ? current.results.map((item) => (item.criterionId === criterionId ? latest : item))
          : [...current.results, latest];
        return { personId: current.personId, results: nextResults };
      });
    } catch {
      // keep previous criterion result if refresh fails
    }
  }

  async resetProgress(criterionId: string): Promise<void> {
    await this.persistProgress(criterionId, emptyProgressPayload(criterionId));
  }
}
