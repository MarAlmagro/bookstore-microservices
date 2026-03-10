import { test, expect } from '@playwright/test';

test.describe('Admin UI', () => {
  test('should display login page', async ({ page }) => {
    await page.goto('/login');
    await expect(page.getByTestId('login-form')).toBeVisible();
    await expect(page.getByTestId('login-username')).toBeVisible();
    await expect(page.getByTestId('login-password')).toBeVisible();
  });

  test('should login successfully', async ({ page }) => {
    await page.goto('/login');
    await page.getByTestId('login-username').fill('admin');
    await page.getByTestId('login-password').fill('admin123');
    await page.getByTestId('login-submit').click();
    await expect(page).toHaveURL(/dashboard/);
    await expect(page.getByTestId('stat-books')).toBeVisible();
  });

  test('should navigate to books list', async ({ page }) => {
    await page.goto('/login');
    await page.getByTestId('login-username').fill('admin');
    await page.getByTestId('login-password').fill('admin123');
    await page.getByTestId('login-submit').click();
    
    await page.getByTestId('nav-books').click();
    await expect(page.getByTestId('books-table')).toBeVisible();
  });
});
