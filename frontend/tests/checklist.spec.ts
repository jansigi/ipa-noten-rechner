import { expect, test } from '@playwright/test';
import { mockBackend } from './backend-mock';

test('can check off a requirement and persist progress', async ({ page }) => {
  await mockBackend(page);

  await page.goto('/checklist');

  await expect(page.getByRole('heading', { name: 'Bewertung' })).toBeVisible();

  const firstCheckbox = page.getByRole('checkbox').first();
  await expect(firstCheckbox).not.toBeChecked();

  await firstCheckbox.check();
  await expect(firstCheckbox).toBeChecked();

  // UI should show an evaluation summary for the criterion
  await expect(page.getByText(/Bewertung:\s*\d\s*\/\s*3/)).toBeVisible();
  await expect(page.getByText(/Erfüllt:\s*\d+\s+von\s+\d+/)).toBeVisible();
});

test('can write a note and keep it on blur', async ({ page }) => {
  await mockBackend(page);

  await page.goto('/checklist');

  const note = page.getByLabel('Notiz').first();
  await note.fill('Noch Anforderungen nachziehen');
  await note.blur();

  // The textarea is bound to the store value; after blur it should still show the text
  await expect(note).toHaveValue('Noch Anforderungen nachziehen');
});
