import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
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

  readonly loading = this.store.criteriaLoading;
  readonly error = this.store.error;
  readonly criteria = this.store.criteria;

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

}
