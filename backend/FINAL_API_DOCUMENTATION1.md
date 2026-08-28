# Gym Management System API --- Final QA & Postman Documentation

## Final status

Final Postman Collection Runner result:

-   Total tests: **58**
-   Passed: **58**
-   Failed: **0**
-   Skipped: **0**
-   Errors: **0**
-   Pass rate: **100%**

This is the current regression/acceptance baseline.

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

Login requests obtain the JWT and save it to `jwt_token`.

Trainer login is expected to return HTTP 200, a string `token`, and role
`TRAINER`.

Member login is expected to return HTTP 200, a string `token`, and role
`MEMBER`.

## Important UUID distinction

The project must distinguish between:

-   authentication `userUuid`
-   trainer profile `trainerUuid`
-   member `memberUuid`
-   gym class `classUuid`
-   booking `bookingUuid`

For gym class creation, `trainerUuid` must refer to the **Trainer
profile UUID** returned by the trainer-profile endpoint, not merely the
authentication `userUuid`.

## Gym Classes

### Create

``` http
POST {{base_url}}/gym-classes
```

Example:

``` json
{
  "name": "Morning Yoga",
  "trainerUuid": "{{trainer_uuid}}",
  "capacity": 15,
  "dateTime": "2026-09-26T10:00:00"
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

``` json
{
  "name": "Booking Test Class",
  "trainerUuid": "{{trainer_uuid}}",
  "capacity": 15,
  "dateTime": "2026-09-26T20:00:00"
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

The current implementation is externally verified when the full
collection reports:

``` text
Passed: 58
Failed: 0
Skipped: 0
Errors: 0
Pass rate: 100%
```

Any future backend change should rerun the complete collection and
compare against this 58/58 baseline.

The Postman result is evidence of API behavior, but it does not by
itself prove that the implementation is architecturally correct. Cursor
should therefore inspect the actual source code separately.
