import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { login as loginApi } from '../services/authService';
import { handleApiError } from '../lib/api';
import type { LoginRequest, Role } from '../types/api';

interface DemoAccount {
  name: string;
  email: string;
  role: Role;
}

const DEMO_ACCOUNTS: DemoAccount[] = [
  { name: 'Alex Admin', email: 'admin@gym.com', role: 'ADMIN' },
  { name: 'Taylor Trainer', email: 'trainer@gym.com', role: 'TRAINER' },
  { name: 'Morgan Member', email: 'member@gym.com', role: 'MEMBER' },
];

export function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [formData, setFormData] = useState<LoginRequest>({
    email: '',
    password: '',
  });
  const [error, setError] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const response = await loginApi(formData);
      login(response);
      navigate('/dashboard');
    } catch (err) {
      const apiError = handleApiError(err);
      setError(apiError.message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDemoClick = (email: string) => {
    setFormData({ email, password: 'password123' });
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '1rem' }}>
      <div style={{ maxWidth: '440px', width: '100%' }}>
        <div className="card">
          <div className="card-header" style={{ textAlign: 'center', paddingBottom: '1.5rem' }}>
            <div style={{
              width: '48px',
              height: '48px',
              margin: '0 auto 1rem',
              background: 'var(--color-primary)',
              borderRadius: '50%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'white',
              fontSize: '1.5rem',
              fontWeight: 'bold'
            }}>
              💪
            </div>
            <h1 className="card-title" style={{ fontSize: '1.5rem', marginBottom: '0.5rem' }}>
              Welcome to FitDesk
            </h1>
            <p className="card-subtitle">Sign in to manage classes and bookings</p>
          </div>

          {error && <div className="error-message">{error}</div>}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Email</label>
              <input
                type="email"
                className="form-input"
                placeholder="you@gym.com"
                value={formData.email}
                onChange={(e) =>
                  setFormData({ ...formData, email: e.target.value })
                }
                required
                autoComplete="email"
              />
            </div>

            <div className="form-group">
              <label className="form-label">Password</label>
              <input
                type="password"
                className="form-input"
                placeholder="Enter your password"
                value={formData.password}
                onChange={(e) =>
                  setFormData({ ...formData, password: e.target.value })
                }
                required
                autoComplete="current-password"
              />
            </div>

            <button
              type="submit"
              className="btn btn-primary"
              disabled={isLoading}
              style={{ width: '100%' }}
            >
              {isLoading ? 'Signing in...' : 'Sign In'}
            </button>
          </form>

          <p style={{
            marginTop: '1.5rem',
            textAlign: 'center',
            fontSize: '0.875rem',
            color: 'var(--color-muted)',
          }}>
            Don&apos;t have an account?{' '}
            <Link
              to="/register"
              style={{ color: 'var(--color-primary)', fontWeight: 500, textDecoration: 'none' }}
            >
              Create one
            </Link>
          </p>

          <div style={{ marginTop: '2rem', paddingTop: '1.5rem', borderTop: '1px solid var(--color-border)' }}>
            <p style={{ 
              fontSize: '0.75rem', 
              textTransform: 'uppercase', 
              letterSpacing: '0.05em', 
              color: 'var(--color-muted)',
              marginBottom: '0.75rem'
            }}>
              Demo Accounts
            </p>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {DEMO_ACCOUNTS.map((account) => (
                <button
                  key={account.email}
                  type="button"
                  onClick={() => handleDemoClick(account.email)}
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '0.75rem',
                    border: '1px solid var(--color-border)',
                    borderRadius: 'var(--radius)',
                    background: 'transparent',
                    cursor: 'pointer',
                    textAlign: 'left',
                    transition: 'background 0.2s'
                  }}
                  onMouseEnter={(e) => e.currentTarget.style.background = 'var(--color-secondary)'}
                  onMouseLeave={(e) => e.currentTarget.style.background = 'transparent'}
                >
                  <div>
                    <div style={{ fontWeight: 500, fontSize: '0.875rem', marginBottom: '0.125rem' }}>
                      {account.name}
                    </div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--color-muted)' }}>
                      {account.email}
                    </div>
                  </div>
                  <span className="badge badge-secondary">{account.role}</span>
                </button>
              ))}
            </div>
            <p style={{ 
              fontSize: '0.75rem', 
              color: 'var(--color-muted)', 
              marginTop: '0.75rem',
              textAlign: 'center'
            }}>
              All demo accounts use password: <code style={{ 
                background: 'var(--color-secondary)', 
                padding: '0.125rem 0.375rem', 
                borderRadius: '0.25rem',
                fontFamily: 'monospace'
              }}>password123</code>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
