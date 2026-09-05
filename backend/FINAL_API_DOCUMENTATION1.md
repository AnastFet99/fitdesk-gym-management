# Gym Management System API --- Final QA & Postman Documentation

## Final status

**Manually verified for this submission:**

- Postman requests **1.1–6.6** ran successfully
- Postman **7.1–7.2** are complete (unauthorized / invalid token → 401)

There is **no saved Collection Runner report** in the repository for a current 58/58 run.

A **historical** documented Collection Runner result (not stored as an export in git) was:

-   Total tests: **58**
-   Passed: **58**
-   Failed: **0**
-   Skipped: **0**
-   Errors: **0**
-   Pass rate: **100%**

Treat that 58/58 line as **historical documentation only**, not as proof of a saved report.

Canonical collection: `postman/Gym_Management_API.postman_collection.json`

## Current working Postman order (start)

### 1.1 Register Admin

``` http
POST {{base_url}}/auth/register
```

No Bearer token. Creates an **ADMIN** user and returns a JWT. Save `token` to `jwt_token`.

### 1.2 Register Trainer (create TRAINER **user**)

``` http
POST {{base_url}}/users
```

Authorization: `Bearer {{jwt_token}}` (admin JWT from **1.1**).

This is **`POST /api/users`**, **not** `POST /api/auth/register`. It creates the TRAINER **authentication user**. It does **not** create the trainer **profile**.

### 1.3 Register Member

``` http
POST {{base_url}}/auth/register
```

Creates the MEMBER **user**. Profile is created later (`POST /api/members`).

If collection emails (`admin@gym.com`, `trainer@gym.com`, `member@gym.com`) already exist, register/create-user returns **409**. Use unique emails or an empty database. There is **no** automatic seed.

## Collection variables

Important variables used by the collection:

-   `base_url`
-   `jwt_token`
-   `user_uuid`
-   `trainer_uuid`
-   `member_uuid`
-   `class_uuid`
-   `booking_uuid`
-   `admin_email`
-   `trainer_email`
-   `member_email`

Dynamic IDs/tokens should be stored consistently with:

``` javascript
pm.collectionVariables.set("variable_name", value);
```

## Authentication

Protected endpoints use:

``` text
Bearer Token: {{jwt_token}}
```

Login requests obtain the JWT and save it to `jwt_token`. Demo/test password used in this project: **`password123`**.

Trainer login is expected to return HTTP 200, a string `token`, and role
`TRAINER`.

Member login is expected to return HTTP 200, a string `token`, and role
`MEMBER`.

## Important UUID distinction

| Name | Table | Used for |
|------|--------|----------|
| Authentication **user UUID** | `users.uuid` | Login/register; `userUuid` on create trainer/member **profile** |
| **Trainer profile UUID** | `trainers.uuid` | Gym class field **`trainerUuid`** |
| **Member profile UUID** | `members.uuid` | Booking field **`memberUuid`** |
| Gym class UUID | `gym_classes.uuid` | Booking `gymClassUuid` |
| Booking UUID | `bookings.uuid` | Delete booking |

For gym class creation, `trainerUuid` must be the **trainer profile UUID** from `POST /api/trainers` (stored as `trainer_uuid`), **not** the trainer’s authentication `userUuid`.

For bookings, `memberUuid` must be the **member profile UUID** from `POST /api/members`, **not** the member’s user UUID.

## Gym Classes

### Create

``` http
POST {{base_url}}/gym-classes
```

Example (manually verified date/name):

``` json
{
  "name": "Morning Yoga",
  "trainerUuid": "{{trainer_uuid}}",
  "capacity": 15,
  "dateTime": "2026-10-15T09:00:00"
}
```

Expected: **201 Created**.

The response UUID is stored as:

``` javascript
pm.collectionVariables.set("class_uuid", jsonData.uuid);
```

### Get all

``` http
GET {{base_url}}/gym-classes
```

Expected: **200 OK**, response is an array.

### Get by UUID

``` http
GET {{base_url}}/gym-classes/{{class_uuid}}
```

Expected: **200 OK**.

Response includes `name` as a string and `capacity` as a number.

### Delete

``` http
DELETE {{base_url}}/gym-classes/{{class_uuid}}
```

Expected: **204 No Content**.

## Bookings

### Recreate class for booking test

``` http
POST {{base_url}}/gym-classes
```

Example (manually verified date/name):

``` json
{
  "name": "Evening CrossFit",
  "trainerUuid": "{{trainer_uuid}}",
  "capacity": 10,
  "dateTime": "2026-10-15T18:00:00"
}
```

Expected: **201 Created**.

### Member login

``` http
POST {{base_url}}/auth/login
```

``` json
{
  "email": "{{member_email}}",
  "password": "password123"
}
```

No Bearer token is required for login.

Expected: **200 OK**.

### Create booking

``` http
POST {{base_url}}/bookings
```

Authorization:

``` text
Bearer Token: {{jwt_token}}
```

Body:

``` json
{
  "memberUuid": "{{member_uuid}}",
  "gymClassUuid": "{{class_uuid}}",
  "status": "CONFIRMED"
}
```

Expected: **201 Created**.

Store the returned UUID:

``` javascript
pm.collectionVariables.set("booking_uuid", jsonData.uuid);
```

### Get all bookings

``` http
GET {{base_url}}/bookings
```

Authorization:

``` text
Bearer Token: {{jwt_token}}
```

Expected: **200 OK**, response is an array.

### Get bookings by member

``` http
GET {{base_url}}/bookings/member/{{member_uuid}}
```

Authorization:

``` text
Bearer Token: {{jwt_token}}
```

Expected: **200 OK**, response is an array containing at least one
booking after creation.

If synchronizing the first returned booking:

``` javascript
pm.collectionVariables.set("booking_uuid", jsonData[0].uuid);
```

Use `pm.collectionVariables`, not `pm.environment.set`, for these
collection-level IDs.

### Delete booking

``` http
DELETE {{base_url}}/bookings/{{booking_uuid}}
```

Authorization:

``` text
Bearer Token: {{jwt_token}}
```

Expected: **204 No Content**.

## Test-script conventions

Prefer collection variables consistently:

``` javascript
pm.collectionVariables.set("class_uuid", jsonData.uuid);
pm.collectionVariables.set("booking_uuid", jsonData.uuid);
```

Avoid mixing collection and environment variable storage without a
specific reason.

## Acceptance criteria

For this submission, the API flow is accepted when Postman **1.1–6.6** succeed and **7.1–7.2** return **401** as designed.

A historical documented Runner summary (not a file in this repo) was:

``` text
Passed: 58
Failed: 0
Skipped: 0
Errors: 0
Pass rate: 100%
```

Do not treat that as a current saved report. Re-run the collection after backend changes.

The Postman result is evidence of API behavior, but it does not by
itself prove that the implementation is architecturally correct.
