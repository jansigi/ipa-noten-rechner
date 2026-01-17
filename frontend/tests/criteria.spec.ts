import { expect, test } from '@playwright/test';
import { mockBackend } from './backend-mock';

test('filters criteria by id/title/requirement text', async ({ page }) => {
  await mockBackend(page);

  await page.goto('/kriterien');

  await expect(page.getByRole('heading', { name: 'Bewertungskriterien durchsuchen' })).toBeVisible();

  // One criterion should be visible
  await expect(page.getByText('A01')).toBeVisible();
  await expect(page.getByText('Automatisierung')).toBeVisible();

  // Filter by requirement text
  const search = page.getByPlaceholder('z. B. A01, Dokumentation, Modul BF');
  await search.fill('Tests laufen');

  await expect(page.getByText('A01')).toBeVisible();

  // Filter to an empty result
  await search.fill('does-not-exist');
  await expect(page.getByText('A01')).not.toBeVisible();
});
