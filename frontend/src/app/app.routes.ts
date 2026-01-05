import { Routes } from '@angular/router';
import { AppShellComponent } from './layout/app-shell.component';
import { EvaluationChecklistPageComponent } from './pages/evaluation-checklist/evaluation-checklist.page';

export const routes: Routes = [
  {
    path: '',
    component: AppShellComponent,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'checklist' },
      { path: 'checklist', component: EvaluationChecklistPageComponent, title: 'Checkliste' },
      { path: '**', redirectTo: 'checklist' }
    ]
  }
];
