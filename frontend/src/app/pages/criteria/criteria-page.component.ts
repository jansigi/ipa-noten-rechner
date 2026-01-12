import { ChangeDetectionStrategy, Component, ElementRef, ViewChild, computed, inject, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import { EvaluationStoreService } from '../../services/evaluation-store.service';
import { AppLoadingComponent } from '../../shared/loading/app-loading.component';
import { AppErrorStateComponent } from '../../shared/error-state/app-error-state.component';
import { Criterion } from '../../models/criteria';

@Component({
  selector: 'app-criteria-page',
  standalone: true,
  imports: [
    NgIf,
    NgFor,
    MatCardModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatChipsModule,
    MatButtonModule,
    AppLoadingComponent,
    AppErrorStateComponent
  ],
  templateUrl: './criteria-page.component.html',
  styleUrls: ['./criteria-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CriteriaPageComponent {
  protected readonly store = inject(EvaluationStoreService);
  @ViewChild('fileInput') private fileInput?: ElementRef<HTMLInputElement>;

  readonly loading = this.store.criteriaLoading;
  readonly error = this.store.error;
  readonly criteria = this.store.criteria;
  readonly candidateName = computed(() => {
    const full = this.store.candidateFullName();
    if (full) {
      return full;
    }
    const first = this.store.candidateFirstName();
    const last = this.store.candidateLastName();
    return [first, last].filter((value): value is string => !!value && value.trim().length > 0).join(' ').trim();
  });

  readonly filter = signal('');
  readonly filteredCriteria = computed<Criterion[]>(() => {
    const term = this.filter().trim().toLowerCase();
    const data = this.criteria();
    if (!term) {
      return data;
    }
    return data.filter(
      (criterion) =>
        criterion.id.toLowerCase().includes(term) ||
        criterion.title.toLowerCase().includes(term) ||
        criterion.question.toLowerCase().includes(term) ||
        criterion.requirements.some(
          (req) =>
            req.description.toLowerCase().includes(term) ||
            req.module.toLowerCase().includes(term) ||
            req.id.toLowerCase().includes(term)
        )
    );
  });

  updateFilter(value: string): void {
    this.filter.set(value);
  }

  uploadPdf(): void {
    this.fileInput?.nativeElement.click();
  }

  onFileSelected(event: Event): void {
    const element = event.target as HTMLInputElement | null;
    const file = element?.files?.item(0);
    if (!file) {
      return;
    }
    void this.store.importIpaPdf(file);
    if (element) {
      element.value = '';
    }
  }

  trackCriterion(index: number, criterion: Criterion): string {
    return criterion.id || `${index}`;
  }
}
