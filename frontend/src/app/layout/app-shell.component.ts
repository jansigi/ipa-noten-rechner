import { ChangeDetectionStrategy, Component, computed, inject, signal, ViewChild } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenav, MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { NgFor } from '@angular/common';
import { EvaluationStoreService } from '../services/evaluation-store.service';

type NavItem = {
  label: string;
  path: string;
  icon: string;
};

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    NgFor,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatSidenavModule,
    MatListModule
  ],
  templateUrl: './app-shell.component.html',
  styleUrls: ['./app-shell.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppShellComponent {
  @ViewChild(MatSidenav) private sidenav?: MatSidenav;

  private readonly store = inject(EvaluationStoreService);

  readonly appTitle = computed(() => this.store.ipaName() ?? 'IPA Noten Rechner');
  readonly navItems = signal<NavItem[]>([
    { label: 'Personen', path: '/personen', icon: 'group' },
    { label: 'Kriterien', path: '/kriterien', icon: 'rule' },
    { label: 'Checkliste', path: '/checklist', icon: 'checklist' }
  ]);

  closeSidenav(): void {
    this.sidenav?.close();
  }
}
