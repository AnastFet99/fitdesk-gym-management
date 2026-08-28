# React Frontend Integration Guide

## ✅ CORS Configuration Complete

Your Spring Boot backend is now configured to accept requests from React frontends.

### Allowed Origins (Default)
The backend accepts requests from these origins:
- **http://localhost:3000** - Create React App (CRA) default
- **http://localhost:5173** - Vite default
- **http://localhost:4200** - Angular default

### Custom Origin Configuration
To use a different port or domain, set the environment variable:
```bash
CORS_ORIGINS=http://localhost:3000,https://yourdomain.com
```

Or update `application.properties`:
```properties
cors.allowed.origins=http://localhost:3000,https://yourdomain.com
```

---

## 🚀 Frontend Integration Steps

### 1. API Base URL Configuration

Create a constants file in your React project:

**`src/config/api.js`**:
```javascript
export const API_BASE_URL = 'http://localhost:8080';
export const API_ENDPOINTS = {
  AUTH: {
    REGISTER: `${API_BASE_URL}/api/auth/register`,
    LOGIN: `${API_BASE_URL}/api/auth/login`,
  },
  USERS: `${API_BASE_URL}/api/users`,
  TRAINERS: `${API_BASE_URL}/api/trainers`,
  MEMBERS: `${API_BASE_URL}/api/members`,
  GYM_CLASSES: `${API_BASE_URL}/api/gym-classes`,
  BOOKINGS: `${API_BASE_URL}/api/bookings`,
  SUBSCRIPTIONS: `${API_BASE_URL}/api/subscriptions`,
};
```

### 2. Authentication Service

Create an authentication service to handle registration, login, and token management:

**`src/services/authService.js`**:
```javascript
import { API_ENDPOINTS } from '../config/api';

// Token management
export const getToken = () => {
  return localStorage.getItem('token');
};

export const setToken = (token) => {
  localStorage.setItem('token', token);
};

export const removeToken = () => {
  localStorage.removeItem('token');
};

export const getUserData = () => {
  const userData = localStorage.getItem('userData');
  return userData ? JSON.parse(userData) : null;
};

export const setUserData = (userData) => {
  localStorage.setItem('userData', JSON.stringify(userData));
};

export const removeUserData = () => {
  localStorage.removeItem('userData');
};

export const isAuthenticated = () => {
  return !!getToken();
};

// Register new user
export const register = async (userData) => {
  try {
    const response = await fetch(API_ENDPOINTS.AUTH.REGISTER, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(userData),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Registration failed');
    }

    const data = await response.json();
    
    // Store token and user data
    setToken(data.token);
    setUserData({
      userUuid: data.userUuid,
      name: data.name,
      email: data.email,
      role: data.role,
    });

    return data;
  } catch (error) {
    console.error('Registration error:', error);
    throw error;
  }
};

// Login existing user
export const login = async (credentials) => {
  try {
    const response = await fetch(API_ENDPOINTS.AUTH.LOGIN, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(credentials),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Login failed');
    }

    const data = await response.json();
    
    // Store token and user data
    setToken(data.token);
    setUserData({
      userUuid: data.userUuid,
      name: data.name,
      email: data.email,
      role: data.role,
    });

    return data;
  } catch (error) {
    console.error('Login error:', error);
    throw error;
  }
};

// Logout
export const logout = () => {
  removeToken();
  removeUserData();
};
```

### 3. API Client with JWT

Create a generic API client that automatically includes the JWT token:

**`src/services/apiClient.js`**:
```javascript
import { getToken, logout } from './authService';
import { API_BASE_URL } from '../config/api';

/**
 * Generic API client with automatic JWT token inclusion
 */
export const apiClient = async (endpoint, options = {}) => {
  const token = getToken();
  
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  // Add Authorization header if token exists
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const config = {
    ...options,
    headers,
  };

  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, config);

    // Handle 401 Unauthorized (token expired or invalid)
    if (response.status === 401) {
      logout();
      window.location.href = '/login'; // Redirect to login
      throw new Error('Session expired. Please login again.');
    }

    // Handle 403 Forbidden (insufficient permissions)
    if (response.status === 403) {
      throw new Error('You do not have permission to perform this action.');
    }

    // Handle other errors
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || `Request failed with status ${response.status}`);
    }

    // Return parsed JSON for successful responses
    return await response.json();
  } catch (error) {
    console.error('API Error:', error);
    throw error;
  }
};

// Convenience methods
export const get = (endpoint) => apiClient(endpoint, { method: 'GET' });

export const post = (endpoint, data) =>
  apiClient(endpoint, {
    method: 'POST',
    body: JSON.stringify(data),
  });

export const put = (endpoint, data) =>
  apiClient(endpoint, {
    method: 'PUT',
    body: JSON.stringify(data),
  });

export const del = (endpoint) =>
  apiClient(endpoint, { method: 'DELETE' });
```

### 4. Protected API Calls Examples

Create service functions for each entity:

**`src/services/gymService.js`**:
```javascript
import { get, post, put, del } from './apiClient';

// Gym Classes
export const getAllGymClasses = () => get('/api/gym-classes');

export const getGymClassByUuid = (uuid) => get(`/api/gym-classes/${uuid}`);

export const createGymClass = (gymClassData) => post('/api/gym-classes', gymClassData);

export const updateGymClass = (uuid, gymClassData) => put(`/api/gym-classes/${uuid}`, gymClassData);

export const deleteGymClass = (uuid) => del(`/api/gym-classes/${uuid}`);

// Members
export const getAllMembers = () => get('/api/members');

export const createMember = (memberData) => post('/api/members', memberData);

export const updateMember = (uuid, memberData) => put(`/api/members/${uuid}`, memberData);

// Bookings
export const getBookingsByMember = (memberUuid) => get(`/api/bookings?memberUuid=${memberUuid}`);

export const createBooking = (bookingData) => post('/api/bookings', bookingData);

export const cancelBooking = (uuid) => del(`/api/bookings/${uuid}`);

// Subscriptions
export const getSubscriptionByMember = (memberUuid) => get(`/api/subscriptions?memberUuid=${memberUuid}`);

export const createSubscription = (subscriptionData) => post('/api/subscriptions', subscriptionData);

export const updateSubscription = (uuid, subscriptionData) => put(`/api/subscriptions/${uuid}`, subscriptionData);
```

### 5. React Component Examples

#### Register Component

**`src/components/auth/Register.jsx`**:
```javascript
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { register } from '../../services/authService';

export const Register = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    role: 'MEMBER', // Default role
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await register(formData);
      console.log('Registration successful:', response);
      navigate('/dashboard'); // Redirect after successful registration
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="register-form">
      <h2>Register</h2>
      {error && <div className="error">{error}</div>}
      
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          placeholder="Full Name"
          value={formData.name}
          onChange={(e) => setFormData({ ...formData, name: e.target.value })}
          required
        />
        
        <input
          type="email"
          placeholder="Email"
          value={formData.email}
          onChange={(e) => setFormData({ ...formData, email: e.target.value })}
          required
        />
        
        <input
          type="password"
          placeholder="Password (min 6 characters)"
          value={formData.password}
          onChange={(e) => setFormData({ ...formData, password: e.target.value })}
          required
          minLength={6}
        />
        
        <select
          value={formData.role}
          onChange={(e) => setFormData({ ...formData, role: e.target.value })}
        >
          <option value="MEMBER">Member</option>
          <option value="TRAINER">Trainer</option>
          <option value="ADMIN">Admin</option>
        </select>
        
        <button type="submit" disabled={loading}>
          {loading ? 'Registering...' : 'Register'}
        </button>
      </form>
    </div>
  );
};
```

#### Login Component

**`src/components/auth/Login.jsx`**:
```javascript
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login } from '../../services/authService';

export const Login = () => {
  const navigate = useNavigate();
  const [credentials, setCredentials] = useState({
    email: '',
    password: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await login(credentials);
      console.log('Login successful:', response);
      navigate('/dashboard'); // Redirect after successful login
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-form">
      <h2>Login</h2>
      {error && <div className="error">{error}</div>}
      
      <form onSubmit={handleSubmit}>
        <input
          type="email"
          placeholder="Email"
          value={credentials.email}
          onChange={(e) => setCredentials({ ...credent, email: e.target.value })}
          required
        />
        
        <input
          type="password"
          placeholder="Password"
          value={credentials.password}
          onChange={(e) => setCredentials({ ...credentials, password: e.target.value })}
          required
        />
        
        <button type="submit" disabled={loading}>
          {loading ? 'Logging in...' : 'Login'}
        </button>
      </form>
    </div>
  );
};
```

#### Protected Route Component

**`src/components/auth/ProtectedRoute.jsx`**:
```javascript
import React from 'react';
import { Navigate } from 'react-router-dom';
import { isAuthenticated, getUserData } from '../../services/authService';

export const ProtectedRoute = ({ children, allowedRoles = [] }) => {
  if (!isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }

  // Check role-based access
  if (allowedRoles.length > 0) {
    const userData = getUserData();
    if (!allowedRoles.includes(userData?.role)) {
      return <Navigate to="/unauthorized" replace />;
    }
  }

  return children;
};
```

#### Gym Classes List Component

**`src/components/gymClasses/GymClassList.jsx`**:
```javascript
import React, { useEffect, useState } from 'react';
import { getAllGymClasses } from '../../services/gymService';

export const GymClassList = () => {
  const [gymClasses, setGymClasses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchGymClasses = async () => {
      try {
        const data = await getAllGymClasses();
        setGymClasses(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchGymClasses();
  }, []);

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div className="gym-class-list">
      <h2>Available Classes</h2>
      {gymClasses.map((gymClass) => (
        <div key={gymClass.uuid} className="gym-class-card">
          <h3>{gymClass.name}</h3>
          <p>Trainer: {gymClass.trainerName}</p>
          <p>Specialty: {gymClass.trainerSpecialty}</p>
          <p>Capacity: {gymClass.capacity}</p>
          <p>Date/Time: {new Date(gymClass.dateTime).toLocaleString()}</p>
        </div>
      ))}
    </div>
  );
};
```

### 6. Router Setup with Protected Routes

**`src/App.jsx`**:
```javascript
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Login } from './components/auth/Login';
import { Register } from './components/auth/Register';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import { GymClassList } from './components/gymClasses/GymClassList';
import { Dashboard } from './components/Dashboard';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public routes */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        
        {/* Protected routes */}
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />
        
        <Route
          path="/classes"
          element={
            <ProtectedRoute allowedRoles={['ADMIN', 'TRAINER', 'MEMBER']}>
              <GymClassList />
            </ProtectedRoute>
          }
        />
        
        <Route
          path="/admin"
          element={
            <ProtectedRoute allowedRoles={['ADMIN']}>
              <AdminPanel />
            </ProtectedRoute>
          }
        />
        
        {/* Default redirect */}
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
```

---

## 🧪 Testing Frontend Integration

### Step 1: Start Your React App
```bash
# For Create React App
npm start

# For Vite
npm run dev
```

### Step 2: Test Registration
1. Navigate to `/register`
2. Fill in the form with:
   - Name: "Test User"
   - Email: "test@gym.com"
   - Password: "password123"
   - Role: "MEMBER"
3. Submit → Should receive JWT token and redirect to dashboard

### Step 3: Test Login
1. Navigate to `/login`
2. Enter credentials from registration
3. Submit → Should receive JWT token and redirect

### Step 4: Test Protected Endpoint
1. After login, navigate to `/classes`
2. Component should fetch and display gym classes
3. Check browser Network tab → Should see `Authorization: Bearer <token>` header

### Step 5: Test Token Expiration
1. In browser console: `localStorage.removeItem('token')`
2. Try to access protected route → Should redirect to `/login`

---

## 🔒 Security Best Practices

### ✅ Implemented
- JWT tokens stored in localStorage
- Automatic token inclusion in API requests
- 401 handling with automatic logout
- 403 handling for insufficient permissions
- Role-based route protection

### 🚀 Production Enhancements (Optional)
- Use httpOnly cookies instead of localStorage (requires backend changes)
- Implement token refresh mechanism
- Add HTTPS enforcement
- Implement CSRF protection for state-changing operations
- Add request/response encryption

---

## 📝 API Response Formats

### Authentication Response
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "userUuid": "3827c7ea-b192-4519-bc50-0e75de5ab413",
  "name": "John Doe",
  "email": "john@gym.com",
  "role": "MEMBER"
}
```

### Error Response
```json
{
  "timestamp": "2026-07-30T13:19:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/api/auth/login"
}
```

### Entity Response (e.g., Gym Class)
```json
{
  "uuid": "a1b2c3d4-...",
  "name": "Morning Yoga",
  "trainerUuid": "trainer-uuid-...",
  "trainerName": "Jane Trainer",
  "trainerSpecialty": "Yoga",
  "capacity": 20,
  "dateTime": "2026-08-01T10:00:00"
}
```

---

## 🛠️ Troubleshooting

### CORS Errors
**Problem**: `Access to fetch at 'http://localhost:8080' ... has been blocked by CORS policy`

**Solution**:
1. Check backend is running on port 8080
2. Verify your React dev server port matches allowed origins (3000, 5173, or 4200)
3. If using a custom port, add it to `cors.allowed.origins` in `application.properties`

### 401 Unauthorized
**Problem**: API returns 401 even after login

**Solution**:
1. Check token is stored: `localStorage.getItem('token')`
2. Verify Authorization header is sent: Check Network tab in browser DevTools
3. Token might be expired (24h validity) → Login again

### 403 Forbidden
**Problem**: API returns 403 for certain endpoints

**Solution**:
1. Check user role: `localStorage.getItem('userData')`
2. Verify role has permission for that endpoint (see SecurityConfig)
3. Example: MEMBER cannot access `/api/trainers` POST endpoint

---

## 🎉 You're All Set!

Your React frontend can now:
- ✅ Register new users
- ✅ Login existing users
- ✅ Store JWT tokens
- ✅ Send authenticated requests
- ✅ Handle token expiration
- ✅ Protect routes based on roles
- ✅ Call all CRUD endpoints with proper authorization

Happy coding! 🚀
