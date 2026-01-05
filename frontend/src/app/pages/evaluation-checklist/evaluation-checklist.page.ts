import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  QueryList,
  ViewChildren,
  computed,
  effect,
  inject,
  signal
} from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { EvaluationStoreService } from '../../services/evaluation-store.service';
import { Criterion, Part } from '../../models/evaluation';
import { CustomDropdownComponent, DropdownOption } from '../../shared/custom-dropdown/custom-dropdown.component';

@Component({
  selector: 'app-evaluation-checklist-page',
  standalone: true,
  imports: [
    NgFor,
    NgIf,
    MatCardModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatDividerModule,
    MatProgressBarModule,
    CustomDropdownComponent
  ],
  templateUrl: './evaluation-checklist.page.html',
  styleUrls: ['./evaluation-checklist.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EvaluationChecklistPageComponent {
  @ViewChildren('criterionCard') private criterionCards?: QueryList<ElementRef<HTMLElement>>;

  private readonly store = inject(EvaluationStoreService);

  readonly adminMode = signal(false);
  readonly newCriterionCode = signal('');
  readonly newCriterionTitle = signal('');
  readonly newCriterionPart = signal<string>('teil-1');
  readonly newSubCriterionText = signal('');
  readonly targetCriterionForSub = signal<string>('');

  readonly partSummaries = this.store.partSummaries;
  readonly criteria = this.store.criteria;
  readonly overallGrade = this.store.overallGrade;
  readonly overallRatio = computed(() => {
    const summaries = this.partSummaries();
    const totalAchieved = summaries.reduce((sum, part) => sum + part.achieved, 0);
    const totalMax = summaries.reduce((sum, part) => sum + part.max, 0);
    return totalMax === 0 ? 0 : totalAchieved / totalMax;
  });

  constructor() {
    effect(() => {
      const firstCriterion = this.criteria()[0]?.id;
      if (firstCriterion && !this.targetCriterionForSub()) {
        this.targetCriterionForSub.set(firstCriterion);
      }
    });
  }

  criteriaForPart(part: Part): Criterion[] {
    return this.criteria().filter((c) => c.partId === part.id);
  }

  toggle(subId: string): void {
    this.store.toggleSubcriterion(subId);
  }

  isChecked(subId: string): boolean {
    return this.store.isChecked(subId);
  }

  updateComment(criterionId: string, value: string): void {
    this.store.setComment(criterionId, value);
  }

  commentFor(criterionId: string): string {
    return this.store.commentFor(criterionId);
  }

  jumpTo(criterionId: string): void {
    const element = this.criterionCards
      ?.toArray()
      .find((ref) => ref.nativeElement.dataset['criterionId'] === criterionId)?.nativeElement;
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  reset(): void {
    this.store.resetAll();
  }

  addCriterion(): void {
    const code = this.newCriterionCode().trim() || 'NEW';
    const title = this.newCriterionTitle().trim() || 'Neues Kriterium';
    const partId = this.newCriterionPart();
    this.store.addCriterion(partId, code, title);
    this.newCriterionCode.set('');
    this.newCriterionTitle.set('');
  }

  removeCriterion(criterionId: string): void {
    this.store.removeCriterion(criterionId);
  }

  dropdownOptions(): DropdownOption[] {
    return this.criteria().map((c) => ({ value: c.id, label: `${c.code} — ${c.title}` }));
  }

  partOptions(): DropdownOption[] {
    return this.partSummaries().map((p) => ({ value: p.part.id, label: p.part.name }));
  }

  addSubcriterion(): void {
    const criterionId = this.targetCriterionForSub();
    const text = this.newSubCriterionText().trim();
    if (!criterionId || !text) {
      return;
    }
    this.store.addSubcriterion(criterionId, text, 1);
    this.newSubCriterionText.set('');
  }

  removeLastSub(criterionId: string): void {
    this.store.removeLastSubcriterion(criterionId);
  }

  formatDecimal(value: number, digits = 1): string {
    return value.toFixed(digits).replace('.', ',');
  }

  trackCriterion(index: number, item: Criterion): string {
    return item.id;
  }
}
