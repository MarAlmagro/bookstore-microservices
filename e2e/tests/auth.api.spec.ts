import { test, expect } from '@playwright/test';

test.describe('Authentication API', () => {
  test('should register a new user', async ({ request }) => {
    const response = await request.post('/api/v1/auth/register', {
      data: {
        email: `test-${Date.now()}@example.com`,
        password: 'Test123!',
        firstName: 'Test',
        lastName: 'User',
      },
    });
    expect(response.ok()).toBeTruthy();
    const body = await response.json();
    expect(body.token).toBeDefined();
    expect(body.user.email).toContain('test-');
  });

  test('should login with valid credentials', async ({ request }) => {
    const response = await request.post('/api/v1/auth/login', {
      data: {
        email: 'admin@bookstore.com',
        password: 'admin123',
      },
    });
    expect(response.ok()).toBeTruthy();
    const body = await response.json();
    expect(body.token).toBeDefined();
    expect(body.user.role).toBe('ADMIN');
  });

  test('should reject invalid credentials', async ({ request }) => {
    const response = await request.post('/api/v1/auth/login', {
      data: {
        email: 'admin@bookstore.com',
        password: 'wrongpassword',
      },
    });
    expect(response.status()).toBe(401);
  });
});
