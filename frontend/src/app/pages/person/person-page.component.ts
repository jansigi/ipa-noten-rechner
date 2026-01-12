import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDividerModule } from '@angular/material/divider';
import { EvaluationStoreService } from '../../services/evaluation-store.service';
import { AppLoadingComponent } from '../../shared/loading/app-loading.component';
import { AppErrorStateComponent } from '../../shared/error-state/app-error-state.component';

@Component({
  selector: 'app-person-page',
  standalone: true,
  imports: [
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

}
