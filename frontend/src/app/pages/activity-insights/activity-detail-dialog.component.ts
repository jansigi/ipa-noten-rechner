import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { NgFor, DatePipe, DecimalPipe, NgIf, NgClass } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { Activity } from '../../models/activity';

type ActivityDialogData = {
  activity: Activity;
};

type NumericActivityMetric = 'speed' | 'throughput' | 'quality' | 'focus';

@Component({
  selector: 'app-activity-detail-dialog',
  standalone: true,
  imports: [
    MatDialogModule,
    MatIconModule,
    MatChipsModule,
    MatProgressBarModule,
    MatButtonModule,
    MatDividerModule,
    MatListModule,
    NgFor,
    NgClass,
    NgIf,
    DatePipe,
    DecimalPipe
  ],
  templateUrl: './activity-detail-dialog.component.html',
  styleUrls: ['./activity-detail-dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ActivityDetailDialogComponent {
  private readonly data = inject<ActivityDialogData>(MAT_DIALOG_DATA);

  readonly activity = this.data.activity;

  readonly latestSnapshot = this.activity.snapshots.at(-1) ?? null;
  readonly baselineSnapshot = this.activity.snapshots.at(0) ?? null;

  readonly averageSpeed = this.calculateAverage('speed');
  readonly averageThroughput = this.calculateAverage('throughput');
  readonly averageQuality = this.calculateAverage('quality');

  readonly speedTrend = this.calculateTrend('speed');
  readonly throughputTrend = this.calculateTrend('throughput');
  readonly qualityTrend = this.calculateTrend('quality');

  readonly focusTrend = this.calculateTrend('focus');

  private calculateAverage(metric: NumericActivityMetric): number {
    if (!this.activity.snapshots.length) {
      return 0;
    }
    const sum = this.activity.snapshots.reduce((total, snapshot) => total + snapshot[metric], 0);
    return sum / this.activity.snapshots.length;
  }

  private calculateTrend(metric: NumericActivityMetric): number {
    if (!this.activity.snapshots.length) {
      return 0;
    }
    const first = this.activity.snapshots[0][metric];
    const last = this.activity.snapshots[this.activity.snapshots.length - 1][metric];
    return last - first;
  }

  completionPercent(value: number): number {
    return Math.round(value * 100);
  }

  formatDelta(delta: number): string {
    const sign = delta > 0 ? '+' : '';
    return `${sign}${delta.toFixed(1)}`;
  }

  trendIcon(delta: number): string {
    if (delta > 0.05) {
      return 'trending_up';
    }
    if (delta < -0.05) {
      return 'trending_down';
    }
    return 'trending_flat';
  }

  trendClass(delta: number): string {
    if (delta > 0.05) {
      return 'trend-positive';
    }
    if (delta < -0.05) {
      return 'trend-negative';
    }
    return 'trend-neutral';
  }
}


