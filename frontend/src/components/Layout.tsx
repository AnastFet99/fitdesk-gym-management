import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export function Layout() {
  const { user, isAuthenticated, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path: string) => location.pathname === path;

  if (!isAuthenticated) {
    return <Outlet />;
  }

  return (
    <div className="app-container">
      <header className="header">
        <nav className="nav">
          <div className="nav-brand" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span style={{ fontSize: '1.25rem' }}>💪</span>
            <span style={{ fontWeight: 600 }}>FitDesk</span>
          </div>

          <ul className="nav-links">
            <li>
              <Link
                to="/dashboard"
                className={`nav-link ${isActive('/dashboard') ? 'active' : ''}`}
              >
                Dashboard
              </Link>
            </li>
            <li>
              <Link
                to="/classes"
                className={`nav-link ${isActive('/classes') ? 'active' : ''}`}
              >
                Classes
              </Link>
            </li>
            {user?.role === 'MEMBER' && (
              <li>
                <Link
                  to="/bookings"
                  className={`nav-link ${isActive('/bookings') ? 'active' : ''}`}
                >
                  My Bookings
                </Link>
              </li>
            )}
            <li>
              <span 
                className="nav-link" 
                style={{ 
                  cursor: 'default', 
                  color: 'var(--color-fg)',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem'
                }}
              >
                <span style={{ 
                  fontSize: '0.75rem',
                  padding: '0.125rem 0.5rem',
                  background: 'var(--color-secondary)',
                  borderRadius: '0.25rem',
                  color: 'var(--color-muted)',
                  fontWeight: 500
                }}>
                  {user?.role}
                </span>
                {user?.name}
              </span>
            </li>
            <li>
              <button 
                className="btn btn-secondary" 
                onClick={handleLogout}
                style={{ fontSize: '0.875rem' }}
              >
                Logout
              </button>
            </li>
          </ul>
        </nav>
      </header>

      <Outlet />
    </div>
  );
}
