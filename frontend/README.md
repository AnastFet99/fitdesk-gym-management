# FitDesk — Gym Management Frontend (Final)

> **Full-stack submission guide (start here for grading/setup):**  
> Main README — repository root: `../README.md`
> Backend README: `../backend/README.md`
> Covers architecture, database, env vars, build, deployment, Swagger, Postman (`../backend/postman/Gym_Management_API.postman_collection.json`), and how to run both backend and frontend.

A production-oriented React frontend for the Gym Management System. This project communicates directly with the existing Spring Boot backend and treats the backend/database as the **source of truth**.

## Project Locations

| Component | Path |
|-----------|------|
| **Frontend** | `frontend/` (this directory) |
| **Backend** | `../backend/` |

| Service | URL |
|---------|-----|
| **Frontend** | http://localhost:5173 |
| **Backend API** | http://localhost:8080/api |

## Purpose

FitDesk provides a clean web interface for gym users to:

- Sign in with real backend authentication
- View upcoming gym classes
- Browse class availability
- Book and cancel class reservations (members)

There is **no mock database**, **no mock API fallback**, and **no hardcoded class/booking data**. All displayed state comes from the Spring Boot API.

## Technology Stack

| Layer | Technology |
|-------|------------|
| UI | React 19 + TypeScript |
| Build | Vite 8 |
| Routing | React Router DOM 7 |
| Server state | TanStack React Query 5 |
| HTTP | Axios |
| Styling | Custom CSS (`src/index.css`) |

## Architecture

```
Browser (FitDesk)
    ↓ HTTP + JWT Bearer token
Spring Boot API (localhost:8080/api)
    ↓
MySQL database
```

### Frontend Layers

```
src/
├── pages/           # Login, Dashboard, Classes, Bookings
├── components/      # Layout, ProtectedRoute
├── contexts/        # AuthContext (session state)
├── services/        # API calls (auth, members, gym-classes, bookings)
├── types/           # TypeScript DTOs matching backend
└── lib/api.ts       # Axios client, JWT interceptors, error handling
```

### Design Principles

1. **Backend-first** — DTO field names and endpoints match Spring Boot controllers
2. **Server as source of truth** — booking state is never inferred from local-only state
3. **No mock data** — API failures show errors, not fake data
4. **UUID correctness** — uses `uuid`, `memberUuid`, `gymClassUuid` (not `id`, `classId`, `userId`)

---

## API Documentation

Base URL (configured in `.env`):

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Authenticate user, returns JWT |
| POST | `/api/auth/register` | Register user (Register page at `/register`) |

**Login request:**
```json
{
  "email": "member@gym.com",
  "password": "password123"
}
```

**Login response:**
```json
{
  "token": "<jwt>",
  "type": "Bearer",
  "userUuid": "<uuid>",
  "name": "John Member",
  "email": "member@gym.com",
  "role": "MEMBER"
}
```

### Members

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/members` | List members (backend allows **ADMIN** and **MEMBER** only; not TRAINER) |
| GET | `/api/members/{uuid}` | Get member by UUID |
| POST | `/api/members` | Create member profile |

### Gym Classes

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/gym-classes` | List all gym classes |
| GET | `/api/gym-classes/{uuid}` | Get class by UUID |
| POST | `/api/gym-classes` | Create class |
| DELETE | `/api/gym-classes/{uuid}` | Delete class |

### Bookings

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/bookings` | List all bookings |
| GET | `/api/bookings/member/{memberUuid}` | List bookings for a member |
| POST | `/api/bookings` | Create booking |
| DELETE | `/api/bookings/{uuid}` | Delete/cancel booking |

**Create booking request:**
```json
{
  "memberUuid": "<member-uuid>",
  "gymClassUuid": "<gym-class-uuid>",
  "status": "CONFIRMED"
}
```

**Cancel booking:**
```http
DELETE /api/bookings/{bookingUuid}
```

> **Critical:** Cancellation must use `booking.uuid` (the booking record UUID), **not** `gymClassUuid`.

### Key DTO Fields

| Backend field | Do NOT use |
|---------------|------------|
| `uuid` | `id` |
| `memberUuid` (member **profile** UUID) | member **user** UUID, `userId`, `memberId` |
| `gymClassUuid` | `classId` |
| `userUuid` (auth **user** UUID) | `userId` |
| `trainerUuid` (trainer **profile** UUID, gym classes) | trainer **user** UUID |
| `status` | `bookingStatus` |

**Booking status values:** `PENDING`, `CONFIRMED`, `CANCELLED`

**Roles:** `ADMIN`, `TRAINER`, `MEMBER`

---

## Authentication

### Flow

```
1. User submits email + password on Login page
2. POST /api/auth/login
3. Backend returns JWT + user info
4. Token stored in localStorage ("gym_auth_token")
5. User info stored in localStorage ("gym_user")
6. Axios request interceptor adds: Authorization: Bearer <token>
7. Protected routes require authenticated session
8. 401 response → token removed → redirect to /login
9. Logout → clear token + user → redirect to /login
```

### Demo Accounts

These are **real backend accounts** used through the actual login API (not fake frontend auth):

| Name | Email | Role | Password |
|------|-------|------|----------|
| Alex Admin | admin@gym.com | ADMIN | password123 |
| Taylor Trainer | trainer@gym.com | TRAINER | password123 |
| Morgan Member | member@gym.com | MEMBER | password123 |

Clicking a demo account on the login page prefills email and password, then submits to `POST /api/auth/login`.

> Demo accounts must already exist in the backend database (created via Postman or `POST /api/auth/register`). There is **no** automatic database seed.

---

## Booking Flow

The backend is the **source of truth** for all booking state.

### Verified Member Flow

```
1. Member logs in
2. Frontend loads member profile (GET /api/members → match userUuid)
3. Frontend loads gym classes (GET /api/gym-classes)
4. Frontend loads member bookings (GET /api/bookings/member/{memberUuid})
5. For each class, "Booked" = active booking exists where:
     booking.gymClassUuid === gymClass.uuid
     AND status is CONFIRMED or PENDING
6. User clicks Book
   → POST /api/bookings { memberUuid, gymClassUuid, status: "CONFIRMED" }
   → React Query invalidates ["member-bookings"] and ["bookings"]
   → UI refetches from server → shows "Booked"
7. Browser refresh
   → GET /api/bookings/member/{memberUuid} again
   → still shows "Booked" (server state)
8. User clicks Cancel
   → find active booking for that gymClassUuid
   → DELETE /api/bookings/{booking.uuid}   ← booking UUID, NOT gymClassUuid
   → invalidate queries → UI shows "Available"
9. Browser refresh
   → still shows "Available"
```

### Active Booking Rule

A booking is considered active when:
```typescript
status === 'CONFIRMED' || status === 'PENDING'
```

---

## Bugs Fixed During Development

| Issue | Problem | Final Solution |
|-------|---------|----------------|
| Wrong endpoint | gym-buddy called `/api/classes` | Use `/api/gym-classes` |
| Wrong DTO fields | Used `id`, `classId`, `userId` | Use `uuid`, `gymClassUuid`, `memberUuid` |
| Booking state | UI not derived from backend | Fetch member bookings; derive Booked from active records |
| Cancellation UUID | Used gym class UUID instead of booking UUID | `DELETE /api/bookings/{booking.uuid}` |
| React Query cache | Stale/mismatched cache keys | Invalidate `member-bookings` and `bookings` after mutations |
| Demo email domain | `@gym.test` did not match backend | Changed to `@gym.com` |
| Auth state | Session lost on refresh | Persist token + user in localStorage |
| PowerShell npm | `npm.ps1` execution policy blocked scripts | Session-level `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass` |
| Horizontal overflow | Grid forced 3 columns off-screen | Responsive `repeat(auto-fit, minmax(min(100%, 320px), 1fr))` |
| UI inconsistency | Pages had different spacing/layout | Unified card layout, typography, badges across Dashboard/Classes/Bookings |

---

## Final UI Polish

Visual improvements applied (presentation only — **no functional changes**):

- Responsive grid with no horizontal overflow
- Desktop / tablet / mobile column adaptation
- Compact, scannable cards
- Consistent spacing and typography
- Role badge in navigation
- FitDesk branding on login and navbar
- Consistent Book / Booked / Cancel presentation
- Dashboard, Classes, and My Bookings visually aligned
- Clean white/navy FitDesk styling
- No decorative images or unnecessary assets

---

## Manual Verification

The following were reported as manually verified during development:

| Test | Status |
|------|--------|
| Login with demo accounts | Verified |
| Registration (`/register`) | Verified |
| Dashboard loads classes | Verified |
| Classes page loads | Verified |
| My Bookings page loads | Verified |
| Member profile creation | Verified |
| Book class → "Booked" | Verified |
| Refresh after book → still "Booked" | Verified |
| Cancel → "Available" | Verified |
| Refresh after cancel → still "Available" | Verified |
| TRAINER login and navigation | Verified |
| ADMIN login and navigation | Verified |
| Responsive layout | Verified |
| No horizontal overflow | Verified |

**Not verified:**
- Automated test suite (none exists)
- Postman re-run from frontend changes

---

## Build Verification

Latest build (verified during final audit):

```bash
npm run build
```

| Check | Result |
|-------|--------|
| TypeScript | PASS |
| Vite build | PASS |
| Compilation errors | None |
| Build time | ~636ms |

Output:
```
dist/index.html                   0.46 kB
dist/assets/index-1D594k4T.css    3.62 kB
dist/assets/index-s5l68yri.js   338.18 kB
```

---

## Run Instructions (Windows)

### Prerequisites

- Node.js and npm installed
- Java/Gradle for backend
- Backend environment variables configured (`MYSQL_PASSWORD`, `JWT_SECRET`)

### Terminal 1 — Backend

```powershell
cd ../backend
.\gradlew.bat bootRun
```

Backend must be running at http://localhost:8080 before using the frontend.

### Terminal 2 — Frontend

If PowerShell blocks npm scripts:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
```

Then (from `frontend/`):

```powershell
npm install
npm run dev
```

Open: http://localhost:5173

Both backend and frontend must be running.

---

## Troubleshooting

| Problem | Likely Cause | Solution |
|---------|--------------|----------|
| `npm.ps1` execution policy error | PowerShell script restriction | `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass` |
| Login fails / connection error | Backend not running | Start backend with `.\gradlew.bat bootRun` |
| Frontend blank / API errors | Wrong API URL | Check `.env`: `VITE_API_BASE_URL=http://localhost:8080/api` |
| 401 Unauthorized | Expired/invalid JWT | Log out and log in again |
| "Invalid email or password" | Wrong credentials or user not in DB | Use demo accounts; ensure backend users exist |
| "Already booked" | Active booking exists on server | Cancel existing booking first |
| "Member profile required" | User has no member record | Use "Create Member Profile" on My Bookings |
| Stale UI after action | Browser cache | Hard refresh (Ctrl+F5); booking logic refetches automatically |
| Booking page empty for admin/trainer | Bookings page is member-focused | Log in as `member@gym.com` |

Do not modify backend code for frontend issues unless a genuine backend defect is confirmed.

---

## Known Limitations

- Limited role-specific UI (TRAINER/ADMIN can log in but no dedicated admin/trainer management screens)
- No automated frontend tests
- No offline support (intentional)
- New MEMBER users must create a member profile before booking (supported in-app on My Bookings)
- `react-hook-form` is installed but not used in current pages
- `@tanstack/react-router` is in dependencies but app uses `react-router-dom`

---

## Backend & Postman

This frontend does **not** modify:

- Spring Boot backend source
- Database schema or data
- Postman collection contents

Postman collection location: `../backend/postman/Gym_Management_API.postman_collection.json`

Manually verified: requests **1.1–6.6** succeeded; **7.1–7.2** complete. There is no saved Runner report in the repo proving a current 58/58 run. See `../backend/FINAL_API_DOCUMENTATION1.md` (historical 58/58 note + current 1.1 / 1.2 flow).

---

## Related Documentation

- `FINAL_AUDIT.md` — Final audit, score, and delivery status
- `IMPLEMENTATION_REPORT.md` — Initial implementation report from project creation
