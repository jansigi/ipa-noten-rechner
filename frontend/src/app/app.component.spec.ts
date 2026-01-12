import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideAnimations } from '@angular/platform-browser/animations';
import { beforeEach, describe, expect, it } from 'vitest';
import { AppComponent } from './app.component';
import { EvaluationChecklistPageComponent } from './pages/evaluation-checklist/evaluation-checklist.page';

describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        provideRouter([
          { path: '', pathMatch: 'full', redirectTo: 'checklist' },
          { path: 'checklist', component: EvaluationChecklistPageComponent }
        ]),
        provideAnimations()
      ],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });
});
