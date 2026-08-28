/**
 * Authentication API service.
 * All endpoints verified from backend AuthController.
 */

import { api } from '../lib/api';
import type { LoginRequest, RegisterRequest, AuthResponse } from '../types/api';

/**
 * POST /api/auth/login
 * Authenticates user and returns JWT token.
 */
export async function login(credentials: LoginRequest): Promise<AuthResponse> {
  const response = await api.post<AuthResponse>('/auth/login', credentials);
  return response.data;
}

/**
 * POST /api/auth/register
 * Registers a new user and returns JWT token.
 */
export async function register(data: RegisterRequest): Promise<AuthResponse> {
  const response = await api.post<AuthResponse>('/auth/register', data);
  return response.data;
}
