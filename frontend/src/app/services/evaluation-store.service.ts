import { Injectable, Signal, computed, effect, signal } from '@angular/core';
import {
  Criterion,
  EvaluationDataset,
  Part,
  Subcriterion,
  CriterionId,
  PartId,
  SubcriterionId,
  defaultEvaluationDataset
} from '../models/evaluation';

interface EvaluationState {
  checks: Record<SubcriterionId, boolean>;
  comments: Record<CriterionId, string>;
}

const STORAGE_KEY = 'evaluation-store-v2';
const DATASET_VERSION = 2;

type StoredShape = {
  dataset: EvaluationDataset;
  state: EvaluationState;
  datasetVersion: number;
};

const uid = () =>
  typeof crypto !== 'undefined' && crypto.randomUUID
    ? crypto.randomUUID()
    : `id-${Date.now()}-${Math.random().toString(16).slice(2)}`;

function createEmptyState(dataset: EvaluationDataset): EvaluationState {
  const allSubIds = dataset.criteria.flatMap((c) => c.subcriteria.map((s) => s.id));
  const checks: Record<SubcriterionId, boolean> = {};
  allSubIds.forEach((id) => (checks[id] = false));
  return { checks, comments: {} };
}

function gradeFromRatio(ratio: number): number {
  const clamped = Math.max(0, Math.min(1, ratio));
  return Math.round((1 + clamped * 5) * 10) / 10;
}

@Injectable({ providedIn: 'root' })
export class EvaluationStoreService {
  private readonly dataset = signal<EvaluationDataset>(defaultEvaluationDataset);
  private readonly state = signal<EvaluationState>(createEmptyState(defaultEvaluationDataset));

  readonly parts: Signal<Part[]> = computed(() => this.dataset().parts);
  readonly criteria: Signal<Criterion[]> = computed(() => this.dataset().criteria);

  readonly partSummaries = computed(() =>
    this.parts().map((part) => {
      const criteria = this.criteria().filter((c) => c.partId === part.id);
      const allSub = criteria.flatMap((c) => c.subcriteria);
      const achieved = allSub.reduce((sum, sub) => (this.isChecked(sub.id) ? sum + (sub.points ?? 1) : sum), 0);
      const max = allSub.reduce((sum, sub) => sum + (sub.points ?? 1), 0);
      const ratio = max === 0 ? 0 : achieved / max;
      return {
        part,
        achieved,
        max,
        ratio,
        grade: gradeFromRatio(ratio)
      };
    })
  );

  readonly overallGrade = computed(() => {
    const summaries = this.partSummaries();
    if (!summaries.length) {
      return 0;
    }
    const totalWeight = summaries.reduce((sum, item) => sum + (item.part.weight ?? 1), 0);
    const weighted = summaries.reduce((sum, item) => sum + item.grade * (item.part.weight ?? 1), 0);
    return Math.round((weighted / totalWeight) * 10) / 10;
  });

  constructor() {
    const restored = this.restoreFromStorage();
    if (restored) {
      this.dataset.set(restored.dataset);
      this.state.set(restored.state);
    }

    effect(() => {
      const payload: StoredShape = { dataset: this.dataset(), state: this.state(), datasetVersion: DATASET_VERSION };
      localStorage.setItem(STORAGE_KEY, JSON.stringify(payload));
    });
  }

  isChecked(subId: SubcriterionId): boolean {
    return !!this.state().checks[subId];
  }

  toggleSubcriterion(subId: SubcriterionId): void {
    const current = this.state();
    const nextChecks = { ...current.checks, [subId]: !current.checks[subId] };
    this.state.set({ ...current, checks: nextChecks });
  }

  setComment(criterionId: CriterionId, value: string): void {
    const current = this.state();
    this.state.set({ ...current, comments: { ...current.comments, [criterionId]: value } });
  }

  commentFor(criterionId: CriterionId): string {
    return this.state().comments[criterionId] ?? '';
  }

  addCriterion(partId: PartId, code: string, title: string): void {
    const criteria = this.criteria();
    const newId = uid();
    const newCriterion: Criterion = {
      id: newId,
      code,
      title,
      partId,
      subcriteria: []
    };
    const nextCriteria = [...criteria, newCriterion];
    const parts = this.parts().map((p) =>
      p.id === partId ? { ...p, criteriaIds: [...p.criteriaIds, newId] } : p
    );
    this.dataset.set({ parts, criteria: nextCriteria });
    this.state.set({
      ...this.state(),
      checks: { ...this.state().checks }
    });
  }

  removeCriterion(criterionId: CriterionId): void {
    const criteria = this.criteria().filter((c) => c.id !== criterionId);
    const parts = this.parts().map((p) => ({
      ...p,
      criteriaIds: p.criteriaIds.filter((id) => id !== criterionId)
    }));
    const nextChecks = { ...this.state().checks };
    Object.keys(nextChecks).forEach((id) => {
      if (id.startsWith(criterionId)) {
        delete nextChecks[id];
      }
    });
    const comments = { ...this.state().comments };
    delete comments[criterionId];
    this.dataset.set({ parts, criteria });
    this.state.set({ checks: nextChecks, comments });
  }

  addSubcriterion(criterionId: CriterionId, description: string, points = 1): void {
    const criteria = this.criteria().map((c) => {
      if (c.id !== criterionId) return c;
      const newSub: Subcriterion = {
        id: `${criterionId}-s${c.subcriteria.length + 1}-${uid()}`,
        description,
        points
      };
      return { ...c, subcriteria: [...c.subcriteria, newSub] };
    });
    this.dataset.set({ ...this.dataset(), criteria });
    const checks = { ...this.state().checks };
    const added = criteria.find((c) => c.id === criterionId)?.subcriteria.at(-1);
    if (added) {
      checks[added.id] = false;
    }
    this.state.set({ ...this.state(), checks });
  }

  removeLastSubcriterion(criterionId: CriterionId): void {
    let removedId: SubcriterionId | null = null;
    const criteria = this.criteria().map((c) => {
      if (c.id !== criterionId) return c;
      const trimmed = [...c.subcriteria];
      const removed = trimmed.pop();
      if (removed) {
        removedId = removed.id;
      }
      return { ...c, subcriteria: trimmed };
    });
    if (removedId) {
      const checks = { ...this.state().checks };
      delete checks[removedId];
      this.state.set({ ...this.state(), checks });
    }
    this.dataset.set({ ...this.dataset(), criteria });
  }

  resetAll(): void {
    this.dataset.set(defaultEvaluationDataset);
    this.state.set(createEmptyState(defaultEvaluationDataset));
  }

  private restoreFromStorage(): StoredShape | null {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    try {
      const parsed = JSON.parse(raw) as StoredShape;
      if (parsed.datasetVersion !== DATASET_VERSION) {
        return null;
      }
      return parsed;
    } catch {
      return null;
    }
  }
}
