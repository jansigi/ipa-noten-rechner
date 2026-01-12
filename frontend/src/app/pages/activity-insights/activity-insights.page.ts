import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { NgFor, DatePipe, DecimalPipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Activity, ActivitySnapshot, demoActivityDataset } from '../../models/activity';
import { ActivityDetailDialogComponent } from './activity-detail-dialog.component';

@Component({
  selector: 'app-activity-insights-page',
  standalone: true,
  imports: [
    NgFor,
    DatePipe,
    DecimalPipe,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatChipsModule,
    MatDialogModule
  ],
  templateUrl: './activity-insights.page.html',
  styleUrls: ['./activity-insights.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ActivityInsightsPageComponent {
  private readonly dialog = inject(MatDialog);

  readonly generatedAt = demoActivityDataset.generatedAt;
  readonly activities = signal<Activity[]>(demoActivityDataset.activities);

  readonly averageCompletion = computed(() => {
    const data = this.activities();
    if (!data.length) {
      return 0;
    }
    const total = data.reduce((sum, activity) => sum + this.overallCompletionRatio(activity), 0);
    return total / data.length;
  });

  openDetails(activity: Activity): void {
    this.dialog.open(ActivityDetailDialogComponent, {
      data: { activity },
      width: '720px'
    });
  }

  trackActivity(index: number, activity: Activity): string {
    return activity.id || `${index}`;
  }

  latestSnapshot(activity: Activity): ActivitySnapshot | null {
    const snapshots = activity.snapshots;
    if (!snapshots.length) {
      return null;
    }
    return snapshots[snapshots.length - 1];
  }

  currentSpeed(activity: Activity): number {
    const snapshots = activity.snapshots;
    return snapshots.length ? snapshots[snapshots.length - 1].speed : activity.baselineSpeed;
  }

  speedDelta(activity: Activity): number {
    const snapshots = activity.snapshots;
    if (!snapshots.length) {
      return 0;
    }
    return snapshots[snapshots.length - 1].speed - snapshots[0].speed;
  }

  throughputDelta(activity: Activity): number {
    const snapshots = activity.snapshots;
    if (!snapshots.length) {
      return 0;
    }
    return snapshots[snapshots.length - 1].throughput - snapshots[0].throughput;
  }

  overallCompletionRatio(activity: Activity): number {
    if (!activity.phases.length) {
      return 0;
    }
    const total = activity.phases.reduce((sum, phase) => sum + phase.completionRatio, 0);
    return total / activity.phases.length;
  }

  trendClass(delta: number): string {
    if (delta > 0.05) {
      return 'positive';
    }
    if (delta < -0.05) {
      return 'negative';
    }
    return 'neutral';
  }

  formatDelta(delta: number): string {
    const sign = delta > 0 ? '+' : '';
    return `${sign}${delta.toFixed(1)}`;
  }

  completionPercent(value: number): number {
    return Math.round(value * 100);
  }
}


