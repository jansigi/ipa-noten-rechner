import { expect, test } from '@playwright/test';
import { mockBackend } from './backend-mock';

test('loads the checklist page', async ({ page }) => {
  await mockBackend(page);

  await page.goto('/checklist');
  await expect(page.getByRole('heading', { name: 'Bewertung' })).toBeVisible();
  await expect(page.locator('.brand-name')).toHaveText('IPA Noten Rechner');
});
