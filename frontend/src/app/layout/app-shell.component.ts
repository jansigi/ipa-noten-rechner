import { ChangeDetectionStrategy, Component, signal, ViewChild } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenav, MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { NgFor } from '@angular/common';

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

  readonly appTitle = signal('IPA Noten Rechner');
  readonly navItems = signal<NavItem[]>([
    { label: 'Checkliste', path: '/checklist', icon: 'checklist' }
  ]);

  closeSidenav(): void {
    this.sidenav?.close();
  }
}
