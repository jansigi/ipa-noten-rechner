import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: 'tests',
  timeout: 30_000,
  expect: {
    timeout: 5_000
  },
  reporter: [['junit', { outputFile: 'test-results/junit.xml' }], ['line']],
  use: {
    baseURL: 'http://127.0.0.1:4200'
  },
  webServer: {
    command: 'npm run start -- --host 0.0.0.0 --port 4200',
    url: 'http://127.0.0.1:4200',
    reuseExistingServer: !process.env.CI,
    timeout: 120_000
  }
});
