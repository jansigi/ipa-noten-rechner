import { expect, test } from '@playwright/test';
import { mockBackend } from './backend-mock';

test('creates a new person via the form', async ({ page }) => {
  await mockBackend(page);

  await page.goto('/personen');

  await expect(page.getByRole('heading', { name: 'Personen verwalten' })).toBeVisible();

  await page.getByLabel('Vorname').fill('  Lea  ');
  await page.getByLabel('Nachname').fill('  Muster  ');
  await page.getByLabel('Thema').fill('  DevOps + Testing  ');
  await page.getByLabel('Abgabedatum').fill('2026-01-17');

  await page.getByRole('button', { name: /speichern/i }).click();

  await expect(page.getByText('Lea Muster')).toBeVisible();
});
