# Gym Management System — Full-Stack Submission (Coding Factory 10, AUEB)

Complete gym management application: **Spring Boot REST API** + **MySQL** + **React/TypeScript frontend (FitDesk)**.

This README is the **main entry point** for building, running, testing, and deploying the full submission. Supporting documentation lives in other `.md` files listed at the end; those files are not deleted and remain available for detail.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Architecture](#2-architecture)
3. [Domain Model](#3-domain-model--main-entities)
4. [Backend Layered Architecture](#4-backend-layered-architecture)
5. [Authentication & Authorization](#5-authentication--authorization)
6. [Frontend (FitDesk)](#6-frontend-fitdesk)
7. [Repository / Submission Structure](#7-repository--submission-structure)
8. [Prerequisites](#8-prerequisites)
9. [Database Setup](#9-database-setup)
10. [Environment Variables](#10-environment-variables)
11. [Run the Backend (Development)](#11-run-the-backend-development)
12. [Run the Frontend (Development)](#12-run-the-frontend-development)
13. [Build Instructions](#13-build-instructions)
14. [Build & Deployment](#14-build--deployment)
15. [Swagger (API Documentation)](#15-swagger-api-documentation)
16. [Postman Integration Testing](#16-postman-integration-testing)
17. [Testing & Verification](#17-testing--verification)
18. [Demo Accounts](#18-demo-accounts)
19. [Troubleshooting](#19-troubleshooting)
20. [Additional Documentation](#20-additional-documentation)

---

## 1. Project Overview

The system allows a gym to manage users, trainers, members, gym classes, bookings, and subscriptions.

| Layer | Technology | Location |
|-------|------------|----------|
| **Backend API** | Spring Boot 4.x, Java 21, Gradle | This directory (`backend/`) |
| **Database** | MySQL 8.x | `gymapp` schema on `localhost:3306` |
| **Frontend UI** | React 19, TypeScript, Vite 8 | `../frontend/` |
| **API docs** | SpringDoc OpenAPI (Swagger UI) | `http://localhost:8080/swagger-ui.html` |
| **Integration tests** | Postman collection | `postman/Gym_Management_API.postman_collection.json` |

**Default URLs (local development):**

| Service | URL |
|---------|-----|
| Backend API base | `http://localhost:8080/api` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/api-docs` |
| Frontend (FitDesk) | `http://localhost:5173` |

There is **no cloud deployment configured** in this project. Deployment means preparing a production JAR, a production frontend build, and running them against a MySQL instance with the correct environment variables.

---

## 2. Architecture

```
┌─────────────────────┐         JWT (Bearer)          ┌──────────────────────┐
│  FitDesk Frontend   │  ───────────────────────────►  │  Spring Boot API     │
│  React + TypeScript │         REST / JSON             │  Controllers         │
│  localhost:5173     │  ◄───────────────────────────  │  Services            │
└─────────────────────┘                               │  Repositories        │
                                                        └──────────┬───────────┘
                                                                   │ JPA / Hibernate
                                                                   ▼
                                                        ┌──────────────────────┐
                                                        │  MySQL (gymapp DB)   │
                                                        └──────────────────────┘
```

**Request flow:** Browser → Axios (JWT in `Authorization` header) → REST controllers → service layer → JPA repositories → MySQL.

**Roles:** `ADMIN`, `TRAINER`, `MEMBER` — enforced on the backend; the frontend shows role-appropriate navigation.

---

## 3. Domain Model / Main Entities

Domain-driven entities in `src/main/java/gr/aueb/cf10/gymapp/model/`:

| Entity | Table | Description |
|--------|-------|-------------|
| `User` | `users` | Authentication account (name, email, password, `Role`) |
| `Member` | `members` | 1:1 with `User`; phone; optional `Subscription`; has `Booking`s |
| `Trainer` | `trainers` | 1:1 with `User`; specialty; teaches `GymClass`es |
| `GymClass` | `gym_classes` | Class name, capacity, date/time; belongs to one `Trainer` |
| `Booking` | `bookings` | Links `Member` + `GymClass`; `BookingStatus`; `createdAt` |
| `Subscription` | `subscriptions` | 1:1 with `Member`; plan type and date range |

**Base type:** `AbstractEntity` — internal `Long id` + public `UUID uuid` (APIs use UUIDs).

**Enums:** `Role`, `BookingStatus` (`PENDING`, `CONFIRMED`, `CANCELLED`), `PlanType`.

Hibernate creates/updates tables automatically (`spring.jpa.hibernate.ddl-auto=update`).

---

## 4. Backend Layered Architecture

| Layer | Package / path | Responsibility |
|-------|----------------|----------------|
| **Controllers** | `controller/` | REST endpoints, HTTP status codes, request validation |
| **Services** | `service/` | Business logic, transactions, exceptions |
| **Repositories** | `repository/` | Spring Data JPA persistence |
| **DTOs** | `dto/` | API request/response records (`*InsertDTO`, `*ReadOnlyDTO`) |
| **Mapper** | `core/mapper/Mapper.java` | Entity ↔ DTO conversion |
| **Exceptions** | `core/exceptions/` + `GlobalExceptionHandler.java` | Consistent error responses |
| **Security** | `config/SecurityConfig.java`, `security/` | JWT filter, BCrypt, role-based URL rules |

**REST API groups:**

| Prefix | Controller |
|--------|------------|
| `/api/auth` | `AuthController` — login, register |
| `/api/users` | `UserController` |
| `/api/trainers` | `TrainerController` |
| `/api/members` | `MemberController` |
| `/api/gym-classes` | `GymClassController` |
| `/api/bookings` | `BookingController` |
| `/api/subscriptions` | `SubscriptionController` |

---

## 5. Authentication & Authorization

### Backend

- **Registration:** `POST /api/auth/register` — creates user with BCrypt-hashed password; returns JWT.
- **Login:** `POST /api/auth/login` — validates credentials; returns JWT.
- **JWT:** Stateless; sent as `Authorization: Bearer <token>`; configured via `JWT_SECRET` and `jwt.expiration` (24 h).
- **Password hashing:** BCrypt (`BCryptPasswordEncoder` in `SecurityConfig`).
- **Authorization:** URL-based rules in `SecurityConfig` — e.g. bookings/members for `ADMIN`/`MEMBER`, trainers for `ADMIN`/`TRAINER`, gym-classes readable by all three roles.

### Frontend

- Token stored in `localStorage` (`gym_auth_token`); user profile in `gym_user`.
- `ProtectedRoute` requires authentication for `/dashboard`, `/classes`, `/bookings`.
- Navigation hides **My Bookings** unless `role === 'MEMBER'`.
- Axios interceptor attaches JWT; `401` clears session and redirects to `/login`.

---

## 6. Frontend (FitDesk)

**Path:** `../frontend/` (in this monorepo).

| Feature | Route / behaviour |
|---------|-------------------|
| **Login** | `/login` — `POST /api/auth/login` |
| **Register** | `/register` — `POST /api/auth/register` (MEMBER role) |
| **Dashboard** | `/dashboard` — class overview |
| **Classes** | `/classes` — browse classes, book (members) |
| **My Bookings** | `/bookings` — member bookings; cancel |
| **Member profile** | On My Bookings if no member record — `POST /api/members` with `userUuid` + phone |
| **Book** | `POST /api/bookings` with `memberUuid`, `gymClassUuid`, `status` |
| **Cancel** | `DELETE /api/bookings/{bookingUuid}` — use **booking** UUID, not class UUID |

**DTO / UUID rule:** APIs use public UUIDs, not numeric `id`.

| UUID | Meaning | Typical use |
|------|---------|-------------|
| **User UUID** (`userUuid`) | `users.uuid` — login/register account | `POST /api/trainers` and `POST /api/members` `userUuid` |
| **Trainer profile UUID** | `trainers.uuid` | Gym class `trainerUuid` |
| **Member profile UUID** | `members.uuid` | Booking `memberUuid` |
| **Gym class UUID** | `gym_classes.uuid` | Booking `gymClassUuid`; get/delete class |
| **Booking UUID** | `bookings.uuid` | Cancel/delete booking |

`trainerUuid` on gym-class requests is the **trainer profile** UUID (from `POST /api/trainers`), **not** the trainer’s user UUID. `memberUuid` on bookings is the **member profile** UUID (from `POST /api/members`), **not** the member’s user UUID.

Frontend-specific detail: see `../frontend/README.md`, `../frontend/FINAL_AUDIT.md`, and `../frontend/IMPLEMENTATION_REPORT.md`.

---

## 7. Repository / Submission Structure

```
fitdesk-gym-management/              ← Monorepo root
├── README.md                        ← Main full-stack guide
├── backend/                         ← Backend (this directory)
│   ├── README.md                    ← Backend guide (this file)
│   ├── build.gradle
│   ├── src/main/java/.../gymapp/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── model/
│   │   ├── dto/
│   │   ├── config/
│   │   └── security/
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── postman/
│   │   └── Gym_Management_API.postman_collection.json
│   ├── JWT_TESTING_GUIDE.md
│   ├── FINAL_API_DOCUMENTATION1.md
│   ├── PROJECT_STATUS.md
│   └── ... (other supporting .md files)
└── frontend/
    ├── README.md                    ← Frontend-focused reference
    ├── FINAL_AUDIT.md
    ├── IMPLEMENTATION_REPORT.md
    ├── src/
    └── package.json
```

This repository is a monorepo containing `backend/` and `frontend/`. Graders should begin with the root `README.md`.

---

## 8. Prerequisites

| Tool | Version / notes |
|------|-----------------|
| **Java** | 21 (see `build.gradle` toolchain) |
| **Gradle** | Wrapper included (`gradlew.bat`) |
| **MySQL** | 8.x, service running (e.g. `MySQL80` on Windows) |
| **Node.js + npm** | For frontend (`../frontend/`) |
| **Postman** | Optional — for API integration tests |

---

## 9. Database Setup

1. Start MySQL.
2. Set `MYSQL_PASSWORD` (see [Environment Variables](#10-environment-variables)).
3. The app connects to:

   ```
   jdbc:mysql://localhost:3306/gymapp?createDatabaseIfNotExist=true
   ```

4. On first run, Hibernate creates/updates tables (`ddl-auto=update`).

**Optional manual check:**

```sql
CREATE DATABASE IF NOT EXISTS gymapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Demo users are **not** auto-seeded. There is **no** database seed in this project. Create users via Postman or `POST /api/auth/register`, then create trainer/member **profiles** (`POST /api/trainers`, `POST /api/members`) as needed.

---

## 10. Environment Variables

### Backend (required)

| Variable | Purpose |
|----------|---------|
| `MYSQL_PASSWORD` | MySQL password for user in `application.properties` (default user: `root`) |
| `JWT_SECRET` | Secret key for signing JWTs (use a long random string; do not commit real values) |

**Optional:**

| Variable | Purpose | Default |
|----------|---------|---------|
| `MYSQL_USER` | MySQL username | `root` |
| `CORS_ORIGINS` | Allowed frontend origins (comma-separated) | `http://localhost:3000,http://localhost:5173,...` |

**PowerShell (current session):**

```powershell
$env:MYSQL_PASSWORD = "<your_mysql_password>"
$env:JWT_SECRET = "<your_jwt_secret>"
```

### Frontend

Create `.env` in `../frontend/` (see `.env.example`):

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

For production builds, set `VITE_API_BASE_URL` to your deployed API URL **before** running `npm run build`.

---

## 11. Run the Backend (Development)

```powershell
$env:MYSQL_PASSWORD = "<your_mysql_password>"
$env:JWT_SECRET = "<your_jwt_secret>"
.\gradlew.bat bootRun
```

**Expected:** `Tomcat started on port 8080` (or the port you configure).

API base: `http://localhost:8080/api`

---

## 12. Run the Frontend (Development)

Start the backend first.

```powershell
cd ../frontend
npm install
npm run dev
```

If PowerShell blocks npm scripts:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
```

Open: `http://localhost:5173`

---

## 13. Build Instructions

### Backend — executable JAR

```powershell
$env:MYSQL_PASSWORD = "<your_mysql_password>"
$env:JWT_SECRET = "<your_jwt_secret>"
.\gradlew.bat clean bootJar
```

Output JAR (typical path):

```
build/libs/gymapp-0.0.1-SNAPSHOT.jar
```

### Backend — run tests (JUnit)

```powershell
.\gradlew.bat test
```

Currently includes Spring Boot context load test (`GymappApplicationTests`).

### Frontend — production static build

```powershell
cd ../frontend
npm install
npm run build
```

Output:

```
dist/
├── index.html
└── assets/
```

---

## 14. Build & Deployment

> **Assignment note:** This section describes how the application is **built and prepared for deployment**. The project is **not** pre-deployed to AWS, Azure, or similar; follow these steps for a realistic production setup.

### What you deploy

| Component | Artifact | Runtime needs |
|-----------|----------|---------------|
| Backend | `gymapp-0.0.1-SNAPSHOT.jar` | Java 21, MySQL, env vars |
| Frontend | `dist/` static files | Web server or static host |
| Database | MySQL `gymapp` schema | Persistent MySQL 8.x instance |

### Step 1 — Prepare MySQL (production or staging)

1. Create database `gymapp` on your MySQL server.
2. Create a dedicated DB user with least privilege (recommended).
3. Note host, port, username, password.

Update `spring.datasource.url`, `spring.datasource.username` in `application.properties` **or** use externalized configuration (environment-specific properties file on the server). Do not commit production passwords.

### Step 2 — Build and run the backend JAR

```powershell
.\gradlew.bat clean bootJar
```

On the server:

```powershell
$env:MYSQL_PASSWORD = "<production_mysql_password>"
$env:JWT_SECRET = "<production_jwt_secret>"
$env:CORS_ORIGINS = "https://your-frontend-domain.example"
java -jar build/libs/gymapp-0.0.1-SNAPSHOT.jar
```

- Default port: **8080** (override with `server.port` if needed).
- Ensure the firewall allows API access from your frontend origin only where possible.

### Step 3 — Build the frontend for production

Set the API URL to your deployed backend **before** building:

```powershell
# PowerShell example — adjust URL to your backend
$env:VITE_API_BASE_URL = "https://api.your-domain.example/api"
cd ../frontend
npm run build
```

The `dist/` folder contains the production SPA.

### Step 4 — Serve the frontend

Options (choose one):

1. **Static web server** — copy `dist/` to nginx, Apache, IIS, or similar; configure SPA fallback to `index.html`.
2. **Simple local preview** — `npx serve dist` (development/demo only).
3. **Static hosting** — upload `dist/` to any static host that supports client-side routing fallback.

### Step 5 — CORS

Set `CORS_ORIGINS` to your frontend URL(s) so the browser can call the API with credentials/JWT.

### Step 6 — Verify deployment

1. Backend health: open Swagger at `http://<host>:<port>/swagger-ui.html`.
2. Login via API or frontend with a valid user.
3. Run the Postman collection against the deployed `base_url` (update collection variable).
4. Smoke-test member book/cancel flow in the UI.

### Deployment diagram (logical)

```
[Browser] → [Static host: dist/] → API calls → [JAR on Java 21] → [MySQL]
```

No Docker/Kubernetes manifests are included in this submission unless you add them separately.

---

## 15. Swagger (API Documentation)

With the backend running:

| Resource | URL |
|----------|-----|
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **OpenAPI JSON** | http://localhost:8080/api-docs |

All REST controllers are annotated with `@Tag`, `@Operation`, and `@ApiResponses`.

**Try protected endpoints:**

1. `POST /api/auth/login` — copy the `token` from the response.
2. In Swagger UI, click **Authorize**.
3. Enter: `Bearer <your-token>`

---

## 16. Postman Integration Testing

**Collection path (included in this repo):**

```
postman/Gym_Management_API.postman_collection.json
```

**How to run:**

1. Import the JSON file into Postman.
2. Ensure the backend is running (`bootRun` or JAR).
3. Set collection variable `base_url` to `http://localhost:8080/api` (if not already set).
4. Run requests **in collection order** (1.1 → … → 7.2), or use the Collection Runner.

**Current working flow (manually verified):**

| Step | Request | What it does |
|------|---------|----------------|
| **1.1** | `POST /api/auth/register` | Register **ADMIN** (no auth). Save JWT → `jwt_token`. |
| **1.2** | `POST /api/users` | Create **TRAINER user**. Requires `Authorization: Bearer {{jwt_token}}` (admin from 1.1). **Not** `POST /api/auth/register`. |
| **1.3** | `POST /api/auth/register` | Register **MEMBER**. |
| **3.2** | `POST /api/trainers` | Trainer **profile**; save **profile** UUID → `trainer_uuid`. |
| **4.2** | `POST /api/members` | Member **profile**; save **profile** UUID → `member_uuid`. |
| **5.2 / 6.1** | `POST /api/gym-classes` | Use `trainer_uuid` (profile). |
| **6.3** | `POST /api/bookings` | Use `member_uuid` (profile) + `class_uuid`. |
| **7.1–7.2** | Unauthorized / invalid token | Expect **401**. |

If `admin@gym.com` / `trainer@gym.com` / `member@gym.com` already exist, 1.1–1.3 will **409**. Use unique emails in collection variables, or use an empty database.

**Manually verified (this submission):** Postman **1.1–6.6** succeeded; **7.1–7.2** are complete. There is **no saved Collection Runner report** in the repository proving a current 58/58 run. `FINAL_API_DOCUMENTATION1.md` records a **historical** documented Runner result of 58/58; treat that as historical, not as a stored artifact.

---

## 17. Testing & Verification

| Type | Location | Notes |
|------|----------|-------|
| **Postman integration** | `postman/Gym_Management_API.postman_collection.json` | 1.1–6.6 manually verified; 7.1–7.2 complete |
| **JUnit** | `src/test/java/.../GymappApplicationTests.java` | Context load smoke test |
| **Swagger manual** | Swagger UI | Interactive endpoint testing |
| **Frontend manual** | FitDesk UI | Login, register, book, cancel — see `../frontend/FINAL_AUDIT.md` |

**Suggested verification order:**

1. `.\gradlew.bat test`
2. Start backend → Postman collection in order (1.1–6.6, then 7.1–7.2)
3. Start frontend → demo member flow (login → classes → book → refresh → cancel)

---

## 18. Demo Accounts

Use these **only if the corresponding users already exist** in MySQL. The project does **not** seed them automatically. Create them with Postman or `POST /api/auth/register` (and `POST /api/users` for the trainer user in the collection flow).

| Name | Email | Role | Password |
|------|-------|------|----------|
| Alex Admin | `admin@gym.com` | ADMIN | `password123` |
| Taylor Trainer | `trainer@gym.com` | TRAINER | `password123` |
| Morgan Member | `member@gym.com` | MEMBER | `password123` |

The FitDesk login page can prefill these credentials. New registrations via `/register` create **MEMBER** users; they must create a member profile on **My Bookings** before booking.

---

## 19. Troubleshooting

| Problem | Likely cause | Solution |
|---------|--------------|----------|
| Backend won't start | Missing `JWT_SECRET` or `MYSQL_PASSWORD` | Set both env vars |
| MySQL connection failed | MySQL not running | Start MySQL service; verify credentials |
| Port 8080 in use | Another process on 8080 | Change `server.port` in `application.properties` |
| 401 on API calls | Missing/expired JWT | Login again; check `Authorization: Bearer ...` header |
| Frontend can't reach API | Wrong `VITE_API_BASE_URL` | Set `.env` to `http://localhost:8080/api` |
| CORS errors in browser | Frontend origin not allowed | Add origin to `CORS_ORIGINS` |
| Class create fails | Wrong trainer reference | Use **trainer profile** UUID (`trainers.uuid`), not the user UUID from login/register |
| Cancel booking fails | Wrong UUID | Use `booking.uuid`, not `gymClassUuid` |
| npm script blocked (Windows) | PowerShell execution policy | `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass` |

### Example: create gym class (correct DTO)

```json
{
  "name": "Morning Yoga",
  "trainerUuid": "<trainer-profile-uuid>",
  "capacity": 20,
  "dateTime": "2026-07-28T10:00:00"
}
```

Endpoint: `POST /api/gym-classes`

---

## 20. Additional Documentation

Supporting files (kept for reference; **this README is authoritative for setup/submission**):

| File | Purpose |
|------|---------|
| `JWT_TESTING_GUIDE.md` | JWT and role-based API testing |
| `FINAL_API_DOCUMENTATION1.md` | API QA notes and historical Postman Runner notes |
| `PROJECT_STATUS.md` | Development checklist (some items may be outdated) |
| `SPRING_SECURITY_IMPLEMENTATION.md` | Security design notes |
| `REACT_INTEGRATION_GUIDE.md` | Frontend integration reference |
| `HELP.md` | Spring Boot / Gradle reference links |
| `../frontend/README.md` | Frontend architecture and API field reference |
| `../frontend/FINAL_AUDIT.md` | Frontend final audit |
| `../frontend/IMPLEMENTATION_REPORT.md` | Initial frontend implementation report |

**Known stale content in older docs:** `PROJECT_STATUS.md` may still list Postman as TODO. Prefer this README, the root `README.md`, and `FINAL_API_DOCUMENTATION1.md` for setup and Postman order.

---

## Coding Factory 10 — Requirements Checklist

| Requirement | Status |
|-------------|--------|
| Domain model + database | ✅ |
| Layered backend (Controller / Service / Repository) | ✅ |
| REST API | ✅ |
| React frontend | ✅ (`../frontend/`) |
| Authentication & authorization (backend + frontend) | ✅ |
| Testing (JUnit + Postman) | ✅ Postman 1.1–6.6 + 7.1–7.2 (manual); minimal JUnit |
| Swagger documentation | ✅ |
| README build + deploy | ✅ (this document) |
| GitHub / portfolio ready | Submit this monorepo (`backend/`, `frontend/`, and root `README.md`) |

---

*Coding Factory 10 — Athens University of Economics and Business (AUEB)*
