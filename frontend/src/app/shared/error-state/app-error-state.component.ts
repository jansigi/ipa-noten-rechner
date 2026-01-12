import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-error-state',
  standalone: true,
  imports: [MatCardModule, MatButtonModule, MatIconModule],
  templateUrl: './app-error-state.component.html',
  styleUrls: ['./app-error-state.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppErrorStateComponent {
  @Input() title = 'Es ist ein Fehler aufgetreten';
  @Input() message = 'Bitte versuche es in wenigen Augenblicken erneut.';
  @Input() details?: string;
  @Input() retryLabel = 'Erneut versuchen';
  @Output() readonly retry = new EventEmitter<void>();
}
