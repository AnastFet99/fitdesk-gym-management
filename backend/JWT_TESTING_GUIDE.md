# Spring Security + JWT Testing Guide

## Authentication Flow

Your Gym Management System now includes full JWT-based authentication and authorization with role-based access control.

### 1. Register a New User

**Endpoint**: `POST /api/auth/register`

**Request Body**:
```json
{
  "name": "Admin User",
  "email": "admin@gym.com",
  "password": "admin123",
  "role": "ADMIN"
}
```

**Response** (201 Created):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "userUuid": "3827c7ea-b192-4519-bc50-0e75de5ab413",
  "name": "Admin User",
  "email": "admin@gym.com",
  "role": "ADMIN"
}
```

### 2. Login

**Endpoint**: `POST /api/auth/login`

**Request Body**:
```json
{
  "email": "admin@gym.com",
  "password": "admin123"
}
```

**Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "userUuid": "3827c7ea-b192-4519-bc50-0e75de5ab413",
  "name": "Admin User",
  "email": "admin@gym.com",
  "role": "ADMIN"
}
```

### 3. Using the Token

Copy the `token` value from the registration or login response, then:

**In Swagger UI**:
1. Click the **"Authorize"** button at the top right
2. In the dialog, enter: `Bearer <your-token-here>`
3. Click "Authorize"
4. Now all protected endpoints will use this token

**In Postman/Curl**:
Add this header to your requests:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## Role-Based Access Control

### Public Endpoints (no authentication required)
- `POST /api/auth/register`
- `POST /api/auth/login`
- `/swagger-ui/**`, `/api-docs/**`

### Protected Endpoints by Role

#### ADMIN Role
- Full access to all endpoints
- Can create/update/delete Users
- Can manage Trainers, Members, Classes, Bookings, and Subscriptions

#### TRAINER Role
- `GET /api/users/**` (read only)
- `GET /api/trainers/**`
- `POST /api/trainers` (create trainer profile)
- `PUT /api/trainers/**` (update trainer profile)
- `GET /api/gym-classes/**`
- `POST /api/gym-classes` (create classes)
- `PUT /api/gym-classes/**` (update classes)
- `DELETE /api/gym-classes/**` (delete classes)

#### MEMBER Role
- `GET /api/users/**` (read only)
- `GET /api/members/**`
- `POST /api/members` (create member profile)
- `PUT /api/members/**` (update member profile)
- `GET /api/gym-classes/**` (view classes)
- `GET /api/bookings/**`
- `POST /api/bookings` (book classes)
- `PUT /api/bookings/**` (update bookings)
- `DELETE /api/bookings/**` (cancel bookings)
- `GET /api/subscriptions/**`
- `POST /api/subscriptions` (subscribe)
- `PUT /api/subscriptions/**` (update subscription)

## Complete Testing Sequence in Swagger

### Step 1: Register Users

1. **Register ADMIN**:
```json
{
  "name": "Admin User",
  "email": "admin@gym.com",
  "password": "admin123",
  "role": "ADMIN"
}
```
Copy the `token` from the response.

2. **Authorize in Swagger**: Click "Authorize" → Enter `Bearer <admin-token>`

3. **Register TRAINER** (using admin token):
```json
{
  "name": "John Trainer",
  "email": "trainer@gym.com",
  "password": "trainer123",
  "role": "TRAINER"
}
```

4. **Register MEMBER** (using admin token):
```json
{
  "name": "Jane Member",
  "email": "member@gym.com",
  "password": "member123",
  "role": "MEMBER"
}
```

### Step 2: Login as Trainer

1. **POST /api/auth/login**:
```json
{
  "email": "trainer@gym.com",
  "password": "trainer123"
}
```

2. Copy the trainer's `userUuid` and `token`
3. **Authorize again**: `Bearer <trainer-token>`

### Step 3: Create Trainer Profile

**POST /api/trainers**:
```json
{
  "userUuid": "<trainer-user-uuid>",
  "specialty": "Yoga & Pilates"
}
```

Copy the `trainerUuid` from response.

### Step 4: Create Gym Class

**POST /api/gym-classes** (as trainer):
```json
{
  "name": "Morning Yoga",
  "trainerUuid": "<trainer-uuid>",
  "capacity": 15,
  "dateTime": "2026-08-01T08:00:00"
}
```

Copy the `gymClassUuid`.

### Step 5: Login as Member

1. **POST /api/auth/login**:
```json
{
  "email": "member@gym.com",
  "password": "member123"
}
```

2. Copy `userUuid` and `token`
3. **Authorize**: `Bearer <member-token>`

### Step 6: Create Member Profile

**POST /api/members**:
```json
{
  "userUuid": "<member-user-uuid>",
  "phone": "+30 6912345678"
}
```

Copy the `memberUuid`.

### Step 7: Create Subscription

**POST /api/subscriptions**:
```json
{
  "memberUuid": "<member-uuid>",
  "planType": "MONTHLY",
  "startDate": "2026-08-01",
  "endDate": "2026-09-01"
}
```

### Step 8: Book a Class

**POST /api/bookings**:
```json
{
  "memberUuid": "<member-uuid>",
  "gymClassUuid": "<gym-class-uuid>",
  "status": "CONFIRMED"
}
```

### Step 9: Test Authorization

1. Try accessing `POST /api/users` with the **member token** → Should get **403 Forbidden**
2. Try accessing `POST /api/bookings` with the **trainer token** → Should get **403 Forbidden**
3. Switch back to **admin token** → All endpoints should work

## Security Features Implemented

✅ **BCrypt Password Hashing** - All passwords are securely hashed  
✅ **JWT Token Generation** - Tokens expire after 24 hours (86400000ms)  
✅ **Token Validation Filter** - Automatically validates JWT on every request  
✅ **Role-Based Authorization** - `@PreAuthorize` on controllers  
✅ **Stateless Sessions** - No server-side session storage  
✅ **Custom UserDetails** - Integration with Spring Security  
✅ **Authentication Manager** - Handles login validation  
✅ **CORS Ready** - Easy to configure for frontend integration  

## Environment Variables

Add these to your system environment or IDE run configuration:
```
MYSQL_PASSWORD=your_mysql_password
JWT_SECRET=mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLongForHS256Algorithm
```

The JWT secret can be customized via the `JWT_SECRET` environment variable (defaults to a secure value if not set).

## Token Expiration

- **Default**: 24 hours (86400000 milliseconds)
- **Configure**: Change `jwt.expiration` in `application.properties`
- **After expiration**: User must login again to get a new token

## Troubleshooting

### 401 Unauthorized
- Token is missing or invalid
- Token has expired
- Try logging in again

### 403 Forbidden
- User is authenticated but doesn't have the required role
- Check role-based permissions in SecurityConfig.java

### 409 Conflict
- Email already exists during registration
- Use a different email or login with existing credentials
