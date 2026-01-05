/**
 * Frontend TypeScript Models
 * Auto-generated from Java DTOs in the Bookstore Microservices Platform
 * 
 * Type Mapping Rules:
 * - Java Long/Integer -> TypeScript number
 * - Java BigDecimal -> TypeScript number
 * - Java LocalDateTime/Date -> TypeScript string (ISO 8601 format)
 * - Java List/Set -> TypeScript Array
 * - Java String -> TypeScript string
 * - Java Boolean -> TypeScript boolean
 */

/**
 * Book Data Transfer Object
 * Represents a book in the catalog with all its details
 */
export interface BookDTO {
  /**
   * Unique identifier for the book
   */
  id?: number;

  /**
   * International Standard Book Number (ISBN)
   * Must be between 10-13 characters
   * Required field
   */
  isbn: string;

  /**
   * Title of the book
   * Maximum 255 characters
   * Required field
   */
  title: string;

  /**
   * Author of the book
   * Maximum 255 characters
   * Required field
   */
  author: string;

  /**
   * Detailed description of the book
   * Maximum 1000 characters
   * Optional field
   */
  description?: string;

  /**
   * Price of the book
   * Must be greater than 0
   * Required field
   */
  price: number;

  /**
   * Current stock quantity available
   * Must be 0 or greater
   * Required field
   */
  stock: number;

  /**
   * Category or genre of the book
   * Maximum 100 characters
   * Required field
   */
  category: string;
}

/**
 * Order Data Transfer Object
 * Represents a customer order containing one or more items
 */
export interface OrderDTO {
  /**
   * Unique identifier for the order (MongoDB ObjectId)
   * Auto-generated on creation
   */
  id?: string;

  /**
   * Identifier of the user who placed the order
   * Required field
   */
  userId: number;

  /**
   * List of items in the order
   * Must contain at least one item
   * Required field
   */
  items: OrderItemDTO[];

  /**
   * Total amount for the entire order
   * Auto-calculated by the service during order creation
   * Read-only field
   */
  totalAmount?: number;

  /**
   * Current status of the order
   * Valid values: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
   * Auto-set to PENDING during order creation
   */
  status?: OrderStatus;

  /**
   * Timestamp when the order was created
   * ISO 8601 format string
   * Auto-generated on creation
   */
  createdAt?: string;
}

/**
 * Order Item Data Transfer Object
 * Represents a single book item with quantity and price in an order
 */
export interface OrderItemDTO {
  /**
   * Identifier of the book being ordered
   * Required field
   */
  bookId: number;

  /**
   * Quantity of books ordered
   * Must be at least 1
   * Required field
   */
  quantity: number;

  /**
   * Price per unit at the time of order
   * Captured to preserve historical pricing
   * Auto-populated from Catalog Service during order creation
   */
  price?: number;

  /**
   * Title of the book (captured at order time)
   * Auto-populated from Catalog Service during order creation
   */
  bookTitle?: string;

  /**
   * ISBN of the book (captured at order time)
   * Auto-populated from Catalog Service during order creation
   */
  bookIsbn?: string;
}

/**
 * User Data Transfer Object
 * Used for transferring user profile data (without sensitive information like password)
 */
export interface UserDTO {
  /**
   * Unique identifier for the user
   * Auto-generated on creation
   */
  id?: number;

  /**
   * Email address of the user
   * Must be unique and valid email format
   * Maximum 255 characters
   * Required field
   */
  email: string;

  /**
   * First name of the user
   * Maximum 100 characters
   * Required field
   */
  firstName: string;

  /**
   * Last name of the user
   * Maximum 100 characters
   * Required field
   */
  lastName: string;

  /**
   * Role of the user in the system
   * Valid values: CUSTOMER, ADMIN
   * Required field
   */
  role: UserRole;
}

/**
 * Authentication Request Data Transfer Object
 * Contains user credentials for authentication
 */
export interface AuthRequestDTO {
  /**
   * Email address of the user attempting to authenticate
   * Must be valid email format
   * Required field
   */
  email: string;

  /**
   * Password for authentication
   * Minimum length of 6 characters for security
   * Required field
   */
  password: string;
}

/**
 * Authentication Response Data Transfer Object
 * Contains JWT tokens and user information returned after successful authentication
 */
export interface AuthResponseDTO {
  /**
   * JWT access token for authenticating API requests
   * Short-lived token used in Authorization header
   */
  token: string;

  /**
   * Refresh token for obtaining new access tokens
   * Long-lived token used to refresh expired access tokens
   */
  refreshToken: string;

  /**
   * User information for the authenticated user
   * Contains profile details without sensitive data
   */
  user: UserDTO;
}

/**
 * Registration Request Data Transfer Object
 * Used when registering a new user account
 */
export interface RegisterRequestDTO {
  /**
   * Email address for the new account
   * Must be valid email format
   * Required field
   */
  email: string;

  /**
   * Password for the new account
   * Minimum length of 6 characters
   * Required field
   */
  password: string;

  /**
   * First name of the user
   * Required field
   */
  firstName: string;

  /**
   * Last name of the user
   * Required field
   */
  lastName: string;
}

/**
 * Token Refresh Request Data Transfer Object
 * Used when refreshing an expired access token
 */
export interface RefreshTokenRequestDTO {
  /**
   * The refresh token to use for generating a new access token
   * Required field
   */
  refreshToken: string;
}

/**
 * Book Import Data Transfer Object
 * Used for batch import operations
 */
export interface BookImportDTO {
  /**
   * International Standard Book Number
   */
  isbn: string;

  /**
   * Title of the book
   */
  title: string;

  /**
   * Author of the book
   */
  author: string;

  /**
   * Description of the book
   */
  description?: string;

  /**
   * Price of the book
   */
  price: number;

  /**
   * Stock quantity
   */
  stock: number;

  /**
   * Category of the book
   */
  category: string;
}

/**
 * Order Report Data Transfer Object
 * Used for batch reporting operations
 */
export interface OrderReportDTO {
  /**
   * Numeric identifier for the report entry
   */
  numericId: number;

  /**
   * Category of the order
   */
  category: string;

  /**
   * Total amount for the order
   */
  totalAmount: number;

  /**
   * Formatted date string
   */
  formattedDate: string;
}

/**
 * Error Response Data Transfer Object
 * Standard error response format for all microservices
 */
export interface ErrorResponse {
  /**
   * Timestamp when the error occurred
   * ISO 8601 format string
   */
  timestamp: string;

  /**
   * HTTP status code (e.g., 404, 400, 401, 500)
   */
  status: number;

  /**
   * Short error type description (e.g., "Not Found", "Bad Request")
   */
  error: string;

  /**
   * Detailed error message explaining what went wrong
   */
  message: string;

  /**
   * The request path that caused the error
   */
  path: string;
}

/**
 * Batch Operation Response
 * Generic response for batch operations
 */
export interface BatchOperationResponse {
  /**
   * Status of the batch operation (SUCCESS or FAILED)
   */
  status: string;

  /**
   * Descriptive message about the operation
   */
  message: string;

  /**
   * Input file path (for import operations)
   */
  inputFile?: string;

  /**
   * Output file path (for export operations)
   */
  outputFile?: string;

  /**
   * Start date for report operations
   */
  startDate?: string;

  /**
   * End date for report operations
   */
  endDate?: string;
}

// ============================================================================
// ENUMS
// ============================================================================

/**
 * Order Status Enum
 * Represents the various states of an order in the system
 * 
 * Order lifecycle: PENDING → CONFIRMED → SHIPPED → DELIVERED
 * Orders can be CANCELLED at any stage before DELIVERED
 */
export enum OrderStatus {
  /**
   * Order has been created but not yet confirmed
   */
  PENDING = 'PENDING',

  /**
   * Order has been confirmed and is being processed
   */
  CONFIRMED = 'CONFIRMED',

  /**
   * Order has been shipped to the customer
   */
  SHIPPED = 'SHIPPED',

  /**
   * Order has been delivered to the customer
   */
  DELIVERED = 'DELIVERED',

  /**
   * Order has been cancelled
   */
  CANCELLED = 'CANCELLED'
}

/**
 * User Role Enum
 * Represents user roles in the bookstore system
 * Used for role-based access control (RBAC) and authorization
 */
export enum UserRole {
  /**
   * Standard customer role with basic permissions
   * - Can browse books
   * - Can create orders
   * - Can view own profile and orders
   */
  CUSTOMER = 'CUSTOMER',

  /**
   * Administrator role with elevated permissions
   * - All customer permissions
   * - Can manage book catalog
   * - Can view all orders
   * - Can manage users
   */
  ADMIN = 'ADMIN'
}

// ============================================================================
// TYPE GUARDS
// ============================================================================

/**
 * Type guard to check if a value is a valid OrderStatus
 */
export function isOrderStatus(value: any): value is OrderStatus {
  return Object.values(OrderStatus).includes(value);
}

/**
 * Type guard to check if a value is a valid UserRole
 */
export function isUserRole(value: any): value is UserRole {
  return Object.values(UserRole).includes(value);
}

/**
 * Type guard to check if a response is an ErrorResponse
 */
export function isErrorResponse(response: any): response is ErrorResponse {
  return (
    response &&
    typeof response.timestamp === 'string' &&
    typeof response.status === 'number' &&
    typeof response.error === 'string' &&
    typeof response.message === 'string' &&
    typeof response.path === 'string'
  );
}

// ============================================================================
// UTILITY TYPES
// ============================================================================

/**
 * Represents a paginated response wrapper
 * Can be used for future pagination implementation
 */
export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

/**
 * Represents a generic API response wrapper
 */
export interface ApiResponse<T> {
  data?: T;
  error?: ErrorResponse;
  success: boolean;
}

/**
 * Stock update request
 */
export interface StockUpdateRequest {
  quantity: number;
}

/**
 * Order status update request
 */
export interface OrderStatusUpdateRequest {
  status: OrderStatus;
}
