# UI/UX Specification for Bookstore SPA

## Page Structure

### Public Pages (No Auth)
| Route | Page | Components |
|-------|------|------------|
| `/` | Home | Hero, FeaturedBooks, CategoryNav |
| `/login` | Login | LoginForm |
| `/register` | Register | RegisterForm |
| `/books` | Book Catalog | BookGrid, Filters, Pagination |
| `/books/:id` | Book Detail | BookInfo, AddToCart, Reviews |
| `/books/category/:category` | Category View | BookGrid, Breadcrumb |
| `/search` | Search Results | SearchBar, BookGrid |

### Protected Pages (Auth Required)
| Route | Page | Role | Components |
|-------|------|------|------------|
| `/profile` | User Profile | Any | ProfileForm, OrderHistory |
| `/cart` | Shopping Cart | Any | CartItems, CartSummary |
| `/checkout` | Checkout | Any | CheckoutForm, OrderSummary |
| `/orders` | My Orders | Any | OrderList, OrderFilters |
| `/orders/:id` | Order Detail | Any | OrderInfo, OrderItems |

### Admin Pages (ADMIN Role)
| Route | Page | Components |
|-------|------|------------|
| `/admin` | Dashboard | Stats, RecentOrders, LowStock |
| `/admin/books` | Book Management | BookTable, BookForm |
| `/admin/books/new` | Add Book | BookForm |
| `/admin/books/:id/edit` | Edit Book | BookForm |
| `/admin/orders` | Order Management | OrderTable, StatusFilter |
| `/admin/users` | User Management | UserTable |

## Component Specifications

### BookCard
```
┌─────────────────────────┐
│  ┌───────────────────┐  │
│  │                   │  │
│  │   Book Cover      │  │
│  │   (16:9 ratio)    │  │
│  │                   │  │
│  └───────────────────┘  │
│  Category Badge         │
│  Book Title (2 lines)   │
│  Author                 │
│  ★★★★☆ (4.5)           │
│  $29.99    [Add to Cart]│
└─────────────────────────┘

data-testid: book-card-{id}
```

### LoginForm
```
┌─────────────────────────────┐
│         Bookstore           │
│                             │
│  Email                      │
│  ┌───────────────────────┐  │
│  │ user@example.com      │  │
│  └───────────────────────┘  │
│                             │
│  Password                   │
│  ┌───────────────────────┐  │
│  │ ••••••••              │  │
│  └───────────────────────┘  │
│                             │
│  [      Sign In        ]    │
│                             │
│  Don't have an account?     │
│  Register                   │
└─────────────────────────────┘

data-testid: auth-login-form
data-testid: auth-email-input
data-testid: auth-password-input
data-testid: auth-login-submit
```

### CartItem
```
┌──────────────────────────────────────────────┐
│ [img] │ Title                    │ Qty │ $   │
│       │ Author                   │ [-][+]    │
│       │ $19.99 each              │     │ [x] │
└──────────────────────────────────────────────┘

data-testid: cart-item-{bookId}
data-testid: cart-qty-decrease-{bookId}
data-testid: cart-qty-increase-{bookId}
data-testid: cart-remove-{bookId}
```

### OrderStatusBadge
| Status | Color | Icon |
|--------|-------|------|
| PENDING | Yellow | Clock |
| CONFIRMED | Blue | CheckCircle |
| SHIPPED | Purple | Truck |
| DELIVERED | Green | Package |
| CANCELLED | Red | XCircle |

```
data-testid: order-status-badge
```

## Responsive Breakpoints

| Breakpoint | Width | Grid Columns |
|------------|-------|--------------|
| Mobile | < 640px | 1 |
| Tablet | 640-1024px | 2-3 |
| Desktop | > 1024px | 4 |

## Loading States

1. **Skeleton**: Use for initial page loads
2. **Spinner**: Use for button actions
3. **Progress Bar**: Use for multi-step processes

```typescript
// Skeleton pattern
<BookCardSkeleton count={8} />

// Button loading
<Button disabled={isLoading}>
  {isLoading ? <Spinner /> : 'Submit'}
</Button>
```

## Toast Notifications

| Type | Duration | Use Case |
|------|----------|----------|
| Success | 3s | Order placed, item added |
| Error | 5s | API errors, validation |
| Info | 4s | Status updates |
| Warning | 5s | Low stock, session expiring |

## Form Validation Display

```
┌─────────────────────────────┐
│  Email *                    │
│  ┌───────────────────────┐  │
│  │ invalid-email         │  │ ← Red border
│  └───────────────────────┘  │
│  ⚠ Please enter valid email │ ← Error message
└─────────────────────────────┘
```

## Empty States

| Context | Message | Action |
|---------|---------|--------|
| Cart empty | "Your cart is empty" | "Browse Books" button |
| No orders | "No orders yet" | "Start Shopping" button |
| No search results | "No books found for '{query}'" | "Clear filters" link |
| No books in category | "No books in this category" | "View all books" link |

## Keyboard Shortcuts (Optional)

| Key | Action | Context |
|-----|--------|---------|
| `/` | Focus search | Global |
| `Esc` | Close modal | Modal open |
| `Enter` | Submit form | Form focused |
| `←` `→` | Navigate pages | Pagination |

## Color Palette (CSS Variables)

```css
:root {
  /* Primary */
  --primary: 222.2 47.4% 11.2%;
  --primary-foreground: 210 40% 98%;
  
  /* Semantic */
  --success: 142 76% 36%;
  --warning: 38 92% 50%;
  --error: 0 84% 60%;
  --info: 199 89% 48%;
  
  /* Neutral */
  --background: 0 0% 100%;
  --foreground: 222.2 47.4% 11.2%;
  --muted: 210 40% 96%;
  --border: 214.3 31.8% 91.4%;
}
```

## Animation Guidelines

- **Duration**: 150-300ms for micro-interactions
- **Easing**: `ease-out` for entrances, `ease-in` for exits
- **Reduce motion**: Respect `prefers-reduced-motion`

```css
@media (prefers-reduced-motion: reduce) {
  * { animation-duration: 0.01ms !important; }
}
```
