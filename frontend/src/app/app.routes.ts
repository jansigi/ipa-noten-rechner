import { Routes } from '@angular/router';
import { AppShellComponent } from './layout/app-shell.component';
import { EvaluationChecklistPageComponent } from './pages/evaluation-checklist/evaluation-checklist.page';
import { PersonPageComponent } from './pages/person/person-page.component';
import { CriteriaPageComponent } from './pages/criteria/criteria-page.component';

export const routes: Routes = [
  {
    path: '',
    component: AppShellComponent,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'checklist' },
      { path: 'personen', component: PersonPageComponent, title: 'Personen' },
      { path: 'kriterien', component: CriteriaPageComponent, title: 'Kriterien' },
      { path: 'checklist', component: EvaluationChecklistPageComponent, title: 'Checkliste' },
      { path: '**', redirectTo: 'checklist' }
    ]
  }
];
