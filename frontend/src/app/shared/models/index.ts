/** Shared domain & transport types. DB enums are modeled as string unions. */

export type Role = 'CUSTOMER' | 'AGENT' | 'SURVEYOR' | 'MANAGER' | 'ADMIN';

export interface AuthUser {
  id: number;
  username: string;
  fullName: string;
  email?: string;
  role: Role;
  branch?: string | null;
}

export interface LoginResponse {
  token: string;
  user: AuthUser;
}

/** Standard success envelope returned by the API. */
export interface ApiEnvelope<T> {
  data: T;
  correlationId: string;
}

/** Standard error envelope returned by the API. */
export interface ApiError {
  error: { message: string; fields?: Record<string, string> };
  correlationId: string;
}

/** Paginated list payload. */
export interface Paged<T> {
  items: T[];
  page: number;
  size: number;
  total: number;
}

export interface FaqItem {
  q: string;
  a: string;
}
