import { ChangeDetectionStrategy, Component, ElementRef, ViewChild, computed, inject } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDividerModule } from '@angular/material/divider';
import { EvaluationStoreService } from '../../services/evaluation-store.service';
import { Criterion } from '../../models/criteria';
import { CustomDropdownComponent, DropdownOption } from '../../shared/custom-dropdown/custom-dropdown.component';
import { AppLoadingComponent } from '../../shared/loading/app-loading.component';
import { AppErrorStateComponent } from '../../shared/error-state/app-error-state.component';

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
    MatProgressBarModule,
    MatDividerModule,
    CustomDropdownComponent,
    AppLoadingComponent,
    AppErrorStateComponent
  ],
  templateUrl: './evaluation-checklist.page.html',
  styleUrls: ['./evaluation-checklist.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EvaluationChecklistPageComponent {
  protected readonly store = inject(EvaluationStoreService);
  @ViewChild('fileInput') private fileInput?: ElementRef<HTMLInputElement>;

  readonly checklistCriteria = this.store.checklistCriteria;

  readonly personOptions = computed<DropdownOption[]>(() =>
    this.store
      .persons()
      .map((person) => ({
        value: person.id,
        label: `${person.firstName} ${person.lastName}`
      }))
  );

  readonly loading = computed(
    () => this.store.personDataLoading() || this.store.personIpaDatasetLoading() || this.store.personsLoading()
  );

  selectPerson(personId: string): void {
    this.store.selectPerson(personId);
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

  trackCriterion(index: number, item: Criterion): string {
    return item.id;
  }

  isChecked(criterionId: string, requirementId: string): boolean {
    return this.store.isRequirementChecked(criterionId, requirementId);
  }

  toggleRequirement(criterionId: string, requirementId: string): void {
    void this.store.toggleRequirement(criterionId, requirementId);
  }

  noteFor(criterionId: string): string {
    return this.store.noteFor(criterionId);
  }

  updateNote(criterionId: string, value: string): void {
    void this.store.updateNote(criterionId, value);
  }

  resetCriterion(criterionId: string): void {
    void this.store.resetProgress(criterionId);
  }

  formatPercent(value: number): string {
    return `${Math.round(value * 100)}%`;
  }

  formatGrade(value: number): string {
    return value.toFixed(1).replace('.', ',');
  }
}
