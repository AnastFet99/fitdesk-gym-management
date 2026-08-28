/**
 * Authentication context provider.
 * Manages authentication state and user session.
 */

import React, { createContext, useContext, useState, useEffect } from 'react';
import type { AuthResponse, Role } from '../types/api';
import { getToken, setToken, removeToken } from '../lib/api';

interface User {
  uuid: string;
  name: string;
  email: string;
  role: Role;
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (authResponse: AuthResponse) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setTokenState] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Restore session from localStorage
    const storedToken = getToken();
    if (storedToken) {
      setTokenState(storedToken);
      // Try to restore user from localStorage
      const storedUser = localStorage.getItem('gym_user');
      if (storedUser) {
        try {
          setUser(JSON.parse(storedUser));
        } catch {
          // Invalid user data, clear it
          localStorage.removeItem('gym_user');
        }
      }
    }
    setIsLoading(false);
  }, []);

  const login = (authResponse: AuthResponse) => {
    const userData: User = {
      uuid: authResponse.userUuid,
      name: authResponse.name,
      email: authResponse.email,
      role: authResponse.role,
    };
    
    setToken(authResponse.token);
    setTokenState(authResponse.token);
    setUser(userData);
    localStorage.setItem('gym_user', JSON.stringify(userData));
  };

  const logout = () => {
    removeToken();
    setTokenState(null);
    setUser(null);
    localStorage.removeItem('gym_user');
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        isLoading,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
