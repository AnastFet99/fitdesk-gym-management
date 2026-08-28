/**
 * TypeScript types matching the Spring Boot backend DTOs exactly.
 * Generated from backend source code inspection.
 */

// ============================================================================
// ENUMS
// ============================================================================

export type Role = 'ADMIN' | 'TRAINER' | 'MEMBER';

export type BookingStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED';

// ============================================================================
// AUTH DTOs
// ============================================================================

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role: Role;
}

export interface AuthResponse {
  token: string;
  type: string; // "Bearer"
  userUuid: string;
  name: string;
  email: string;
  role: Role;
}

// ============================================================================
// USER DTOs
// ============================================================================

export interface UserReadOnlyDTO {
  uuid: string;
  name: string;
  email: string;
  role: Role;
}

// ============================================================================
// MEMBER DTOs
// ============================================================================

export interface MemberReadOnlyDTO {
  uuid: string;
  userUuid: string;
  userName: string;
  userEmail: string;
  phone: string | null;
  subscriptionUuid: string | null;
}

export interface MemberInsertDTO {
  userUuid: string;
  phone: string;
}

// ============================================================================
// GYM CLASS DTOs
// ============================================================================

export interface GymClassReadOnlyDTO {
  uuid: string;
  name: string;
  trainerUuid: string;
  trainerName: string;
  trainerSpecialty: string;
  capacity: number;
  dateTime: string; // ISO 8601 string
}

export interface GymClassInsertDTO {
  name: string;
  trainerUuid: string;
  capacity: number;
  dateTime: string; // ISO 8601 string
}

// ============================================================================
// BOOKING DTOs
// ============================================================================

export interface BookingReadOnlyDTO {
  uuid: string;
  memberUuid: string;
  memberName: string;
  gymClassUuid: string;
  gymClassName: string;
  classDateTime: string; // ISO 8601 string
  status: BookingStatus;
  createdAt: string; // ISO 8601 string
}

export interface BookingInsertDTO {
  memberUuid: string;
  gymClassUuid: string;
  status?: BookingStatus;
}

// ============================================================================
// ERROR RESPONSES
// ============================================================================

export interface ErrorResponse {
  status: number;
  message: string;
  timestamp: string;
  errors?: Record<string, string[]>;
}
