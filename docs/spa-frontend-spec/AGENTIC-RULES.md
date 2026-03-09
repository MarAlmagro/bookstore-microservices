# Agentic Coding Rules for Bookstore SPA

## Source of Truth

1. **`openapi.yaml`** is the supreme contract. Validate against it before any API integration.
2. **`types.ts`** must be auto-generated from OpenAPI spec using `openapi-typescript`.
3. Never hardcode API paths—import from generated types or constants.

## Project Structure (Recommended)

```
src/
├── api/                    # API layer (auto-generated + custom hooks)
│   ├── generated/          # openapi-typescript output
│   ├── client.ts           # Axios instance with interceptors
│   └── hooks/              # TanStack Query hooks per domain
│       ├── useAuth.ts
│       ├── useBooks.ts
│       ├── useOrders.ts
│       └── useUsers.ts
├── components/
│   ├── ui/                 # Reusable UI primitives (shadcn)
│   └── features/           # Feature-specific components
│       ├── auth/
│       ├── books/
│       ├── orders/
│       └── cart/
├── hooks/                  # Custom React hooks
├── lib/                    # Utilities
│   ├── auth.ts             # Token management
│   └── utils.ts
├── pages/                  # Route pages
├── stores/                 # Zustand stores
│   ├── authStore.ts
│   └── cartStore.ts
├── types/                  # Additional TypeScript types
└── App.tsx
```

## Code Generation Commands

```bash
# Generate TypeScript types from OpenAPI
npx openapi-typescript ./openapi.yaml -o ./src/api/generated/types.ts

# Generate API client (optional, for full client generation)
npx @openapitools/openapi-generator-cli generate \
  -i ./openapi.yaml \
  -g typescript-axios \
  -o ./src/api/generated
```

## API Client Setup

```typescript
// src/api/client.ts
import axios from 'axios';
import { useAuthStore } from '@/stores/authStore';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor: attach token
apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor: handle 401, refresh token
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      const refreshed = await useAuthStore.getState().refreshToken();
      if (refreshed) {
        return apiClient.request(error.config);
      }
      useAuthStore.getState().logout();
    }
    return Promise.reject(error);
  }
);

export default apiClient;
```

## Authentication Store Pattern

```typescript
// src/stores/authStore.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { AuthResponseDto, UserDto } from '@/api/generated/types';

interface AuthState {
  token: string | null;
  refreshToken: string | null;
  user: UserDto | null;
  isAuthenticated: boolean;
  setAuth: (response: AuthResponseDto) => void;
  logout: () => void;
  refreshToken: () => Promise<boolean>;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      refreshToken: null,
      user: null,
      isAuthenticated: false,
      setAuth: (response) => set({
        token: response.token,
        refreshToken: response.refreshToken,
        user: response.user,
        isAuthenticated: true,
      }),
      logout: () => set({
        token: null,
        refreshToken: null,
        user: null,
        isAuthenticated: false,
      }),
      refreshToken: async () => {
        const { refreshToken } = get();
        if (!refreshToken) return false;
        try {
          const res = await apiClient.post('/api/v1/auth/refresh', { refreshToken });
          set({
            token: res.data.token,
            refreshToken: res.data.refreshToken,
          });
          return true;
        } catch {
          return false;
        }
      },
    }),
    { name: 'auth-storage', partialize: (state) => ({ refreshToken: state.refreshToken }) }
  )
);
```

## TanStack Query Hook Pattern

```typescript
// src/api/hooks/useBooks.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from '../client';
import type { BookDto, PageResponseBookDto } from '../generated/types';

export const bookKeys = {
  all: ['books'] as const,
  lists: () => [...bookKeys.all, 'list'] as const,
  list: (filters: object) => [...bookKeys.lists(), filters] as const,
  details: () => [...bookKeys.all, 'detail'] as const,
  detail: (id: number) => [...bookKeys.details(), id] as const,
};

export function useBooks(page = 0, size = 20) {
  return useQuery({
    queryKey: bookKeys.list({ page, size }),
    queryFn: async (): Promise<PageResponseBookDto> => {
      const { data } = await apiClient.get('/api/v1/books/page', {
        params: { page, size },
      });
      return data;
    },
  });
}

export function useBook(id: number) {
  return useQuery({
    queryKey: bookKeys.detail(id),
    queryFn: async (): Promise<BookDto> => {
      const { data } = await apiClient.get(`/api/v1/books/${id}`);
      return data;
    },
    enabled: !!id,
  });
}

export function useCreateBook() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (book: Omit<BookDto, 'id'>): Promise<BookDto> => {
      const { data } = await apiClient.post('/api/v1/books', book);
      return data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: bookKeys.lists() });
    },
  });
}
```

## Error Handling Pattern

```typescript
// src/lib/errorHandler.ts
import type { ErrorResponse } from '@/api/generated/types';
import { AxiosError } from 'axios';

export function getErrorMessage(error: unknown): string {
  if (error instanceof AxiosError && error.response?.data) {
    const apiError = error.response.data as ErrorResponse;
    return apiError.message || 'An error occurred';
  }
  return 'Network error';
}

export function isUnauthorized(error: unknown): boolean {
  return error instanceof AxiosError && error.response?.status === 401;
}

export function isForbidden(error: unknown): boolean {
  return error instanceof AxiosError && error.response?.status === 403;
}
```

## Form Validation with Zod

```typescript
// src/lib/validations/auth.ts
import { z } from 'zod';

export const loginSchema = z.object({
  email: z.string().email('Invalid email'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
});

export const registerSchema = z.object({
  email: z.string().email('Invalid email').max(255),
  password: z.string().min(6, 'Password must be at least 6 characters'),
  firstName: z.string().min(1, 'Required').max(100),
  lastName: z.string().min(1, 'Required').max(100),
});

export const bookSchema = z.object({
  isbn: z.string().min(10).max(13),
  title: z.string().min(1).max(255),
  author: z.string().min(1).max(255),
  description: z.string().max(1000).optional(),
  price: z.number().positive(),
  stock: z.number().int().nonnegative(),
  category: z.string().min(1).max(100),
});

export type LoginInput = z.infer<typeof loginSchema>;
export type RegisterInput = z.infer<typeof registerSchema>;
export type BookInput = z.infer<typeof bookSchema>;
```

## Protected Route Pattern

```typescript
// src/components/ProtectedRoute.tsx
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';

interface Props {
  children: React.ReactNode;
  requiredRole?: 'ADMIN' | 'CUSTOMER';
}

export function ProtectedRoute({ children, requiredRole }: Props) {
  const { isAuthenticated, user } = useAuthStore();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (requiredRole && user?.role !== requiredRole && user?.role !== 'ADMIN') {
    return <Navigate to="/unauthorized" replace />;
  }

  return <>{children}</>;
}
```

## Testing Requirements

1. **Unit Tests**: All hooks, stores, and utility functions
2. **Integration Tests**: API client interceptors, auth flow
3. **E2E Tests**: Critical user journeys (login, browse, order)
4. **data-testid Convention**: `[feature]-[element]-[action]`
   - Example: `auth-login-submit`, `book-card-1`, `order-status-badge`

## Accessibility Checklist

- [ ] All interactive elements have visible focus states
- [ ] Images have alt text
- [ ] Forms have proper labels and error announcements
- [ ] Color contrast meets WCAG 2.1 AA
- [ ] Keyboard navigation works for all features
- [ ] ARIA labels on icon-only buttons

## Performance Guidelines

1. Use `React.lazy()` for route-based code splitting
2. Implement virtual scrolling for book lists > 100 items
3. Use `staleTime` in TanStack Query to reduce refetches
4. Optimize images with lazy loading and proper sizing
5. Prefetch next page data on pagination hover
