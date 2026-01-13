import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api-base-url.token';
import { Person, CreatePersonRequest } from '../models/person';
import { Criterion } from '../models/criteria';
import { CriterionProgress, CriterionProgressRequest } from '../models/progress';
import { EvaluatedCriterion } from '../models/evaluation-result';
import { PersonResults, CriterionResult } from '../models/results';
import { IpaDataset } from '../models/ipa';
import { AppMetadata, IpaImportResponse } from '../models/metadata';

@Injectable({ providedIn: 'root' })
export class BackendApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  getPersons(): Observable<Person[]> {
    return this.http.get<Person[]>(`${this.baseUrl}/persons`);
  }

  createPerson(payload: CreatePersonRequest): Observable<Person> {
    return this.http.post<Person>(`${this.baseUrl}/persons`, payload);
  }

  getCriteria(): Observable<Criterion[]> {
    return this.http.get<Criterion[]>(`${this.baseUrl}/criteria`);
  }

  getProgress(personId: string): Observable<CriterionProgress[]> {
    return this.http.get<CriterionProgress[]>(`${this.baseUrl}/progress/${personId}`);
  }

  saveProgress(personId: string, payload: CriterionProgressRequest): Observable<CriterionProgress> {
    return this.http.post<CriterionProgress>(`${this.baseUrl}/progress/${personId}`, payload);
  }

  getEvaluation(personId: string): Observable<EvaluatedCriterion[]> {
    return this.http.get<EvaluatedCriterion[]>(`${this.baseUrl}/evaluation/${personId}`);
  }

  getResults(personId: string): Observable<PersonResults> {
    return this.http.get<PersonResults>(`${this.baseUrl}/results/${personId}`);
  }

  getCriterionResult(personId: string, criterionId: string): Observable<CriterionResult> {
    return this.http.get<CriterionResult>(`${this.baseUrl}/results/${personId}/${criterionId}`);
  }

  getMetadata(): Observable<AppMetadata> {
    return this.http.get<AppMetadata>(`${this.baseUrl}/metadata`);
  }

  uploadIpaPdf(file: File): Observable<IpaImportResponse> {
    const formData = new FormData();
    formData.append('file', file, file.name);
    return this.http.post<IpaImportResponse>(`${this.baseUrl}/imports/ipa`, formData);
  }

  getIpaDataset(personId: string): Observable<IpaDataset> {
    return this.http.get<IpaDataset>(`${this.baseUrl}/ipa/${personId}`);
  }
}

