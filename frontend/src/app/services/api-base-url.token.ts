import { InjectionToken } from '@angular/core';

type GlobalConfig = {
  IPA_NOTEN_RECHNER_API_URL?: string;
  process?: {
    env?: Record<string, string | undefined>;
  };
  NG_APP_API_URL?: string;
};

export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL', {
  providedIn: 'root',
  factory: () => {
    const globalScope = globalThis as GlobalConfig | undefined;

    if (globalScope?.IPA_NOTEN_RECHNER_API_URL) {
      return globalScope.IPA_NOTEN_RECHNER_API_URL;
    }

    if (globalScope?.NG_APP_API_URL) {
      return globalScope.NG_APP_API_URL;
    }

    const envCandidate = globalScope?.process?.env?.['NG_APP_API_URL'];
    if (envCandidate) {
      return envCandidate;
    }

    return 'http://localhost:8080';
  }
});

