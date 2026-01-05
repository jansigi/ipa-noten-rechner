import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { finalize, of, switchMap, throwError, timer } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AppLoadingComponent } from '../../shared/loading/app-loading.component';
import { AppErrorStateComponent } from '../../shared/error-state/app-error-state.component';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-criteria-page',
  standalone: true,
  imports: [MatCardModule, MatButtonModule, MatIconModule, AppLoadingComponent, AppErrorStateComponent, NgIf],
  templateUrl: './criteria-page.component.html',
  styleUrls: ['./criteria-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CriteriaPageComponent {
  private readonly destroyRef = inject(DestroyRef);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly forceError = signal(false);
  readonly data = signal<string | null>(null);

  constructor() {
    this.loadData();
  }

  toggleError(): void {
    this.forceError.update((value) => !value);
    this.loadData();
  }

  loadData(): void {
    this.loading.set(true);
    this.error.set(null);
    this.data.set(null);

    timer(720)
      .pipe(
        switchMap(() =>
          this.forceError()
            ? throwError(() => new Error('Die Bewertungs-Kriterien konnten nicht geladen werden.'))
            : of('6 Kriterien geladen, Gewichte angepasst.')
        ),
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: (result) => this.data.set(result),
        error: (err) => this.error.set(err.message ?? 'Unbekannter Fehler')
      });
  }
}
