import { expect, test } from '@playwright/test';
import { mockBackend } from './backend-mock';

test('loads the checklist page', async ({ page }) => {
  await mockBackend(page);

  await page.goto('/checklist');
  await expect(page.getByRole('heading', { name: 'Bewertung' })).toBeVisible();
  // The app title is derived from the selected IPA dataset (or falls back).
  await expect(page.locator('.brand-name')).toHaveText(/QV BiVo 2021|IPA Noten Rechner/);
});
