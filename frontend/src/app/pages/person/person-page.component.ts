import { ChangeDetectionStrategy, Component, computed, effect, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDividerModule } from '@angular/material/divider';
import { EvaluationStoreService } from '../../services/evaluation-store.service';
import { Person } from '../../models/person';
import { AppLoadingComponent } from '../../shared/loading/app-loading.component';
import { AppErrorStateComponent } from '../../shared/error-state/app-error-state.component';

@Component({
  selector: 'app-person-page',
  standalone: true,
  imports: [
    NgIf,
    NgFor,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatDividerModule,
    AppLoadingComponent,
    AppErrorStateComponent
  ],
  templateUrl: './person-page.component.html',
  styleUrls: ['./person-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PersonPageComponent {
  protected readonly store = inject(EvaluationStoreService);
  private readonly fb = inject(FormBuilder);

  readonly persons = this.store.persons;
  readonly loading = this.store.personsLoading;
  readonly error = this.store.error;

  readonly form = this.fb.nonNullable.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    topic: ['', Validators.required],
    submissionDate: ['', Validators.required]
  });

  private topicPrefilled = false;
  private firstNamePrefilled = false;
  private lastNamePrefilled = false;
  private lastDatasetId: string | null = null;

  private readonly syncMetadataWithForm = effect(() => {
    const activeDatasetId = this.store.activeDatasetId();
    if (activeDatasetId !== this.lastDatasetId) {
      this.topicPrefilled = false;
      this.firstNamePrefilled = false;
      this.lastNamePrefilled = false;
      this.lastDatasetId = activeDatasetId ?? null;
    }

    const topic = this.store.topic();
    if (topic && !this.topicPrefilled && !this.form.controls.topic.value) {
      this.form.controls.topic.setValue(topic);
      this.topicPrefilled = true;
    }

    const candidateFirst = this.store.candidateFirstName();
    const candidateLast = this.store.candidateLastName();
    const candidateFull = this.store.candidateFullName();

    if (candidateFirst && !this.firstNamePrefilled && !this.form.controls.firstName.value) {
      this.form.controls.firstName.setValue(candidateFirst);
      this.firstNamePrefilled = true;
    } else if (!this.firstNamePrefilled && !this.form.controls.firstName.value && candidateFull) {
      const parts = candidateFull.split(' ').filter(Boolean);
      if (parts.length >= 1) {
        this.form.controls.firstName.setValue(parts[parts.length - 1]);
        this.firstNamePrefilled = true;
      }
    }

    if (candidateLast && !this.lastNamePrefilled && !this.form.controls.lastName.value) {
      this.form.controls.lastName.setValue(candidateLast);
      this.lastNamePrefilled = true;
    } else if (!this.lastNamePrefilled && !this.form.controls.lastName.value && candidateFull) {
      const parts = candidateFull.split(' ').filter(Boolean);
      if (parts.length > 1) {
        this.form.controls.lastName.setValue(parts.slice(0, -1).join(' '));
        this.lastNamePrefilled = true;
      }
    }
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    void this.store.createPerson({
      firstName: value.firstName.trim(),
      lastName: value.lastName.trim(),
      topic: value.topic.trim(),
      submissionDate: value.submissionDate
    });
    this.form.reset();
  }

  trackPerson(index: number, person: Person): string {
    return person.id || `${index}`;
  }
}
