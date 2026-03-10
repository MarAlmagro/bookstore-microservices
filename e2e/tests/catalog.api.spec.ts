import { test, expect } from '@playwright/test';

test.describe('Catalog API', () => {
  test('should list all books', async ({ request }) => {
    const response = await request.get('/api/v1/books');
    expect(response.ok()).toBeTruthy();
    const books = await response.json();
    expect(Array.isArray(books)).toBeTruthy();
  });

  test('should get book by ID', async ({ request }) => {
    const response = await request.get('/api/v1/books/1');
    expect(response.ok()).toBeTruthy();
    const book = await response.json();
    expect(book.id).toBe(1);
    expect(book.title).toBeDefined();
  });

  test('should search books', async ({ request }) => {
    const response = await request.get('/api/v1/books/search?q=java');
    expect(response.ok()).toBeTruthy();
  });

  test('should require auth for creating books', async ({ request }) => {
    const response = await request.post('/api/v1/books', {
      data: {
        isbn: '1234567890123',
        title: 'Test Book',
        author: 'Test Author',
        price: 29.99,
        stock: 10,
      },
    });
    expect(response.status()).toBe(401);
  });
});
