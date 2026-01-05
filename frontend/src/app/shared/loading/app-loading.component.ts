import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-loading',
  standalone: true,
  imports: [MatProgressSpinnerModule, NgIf],
  templateUrl: './app-loading.component.html',
  styleUrls: ['./app-loading.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppLoadingComponent {
  @Input() message = 'Einen Moment, wir laden die Daten...';
}
