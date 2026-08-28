# Cursor Final Review Prompt --- Gym Management System API

First, read the complete file:

`FINAL_API_DOCUMENTATION.md`

Then inspect the entire backend/source project and independently verify
that the implementation genuinely supports the behavior documented
there.

## Known external QA baseline

The final Postman Collection Runner result is:

-   **58 total tests**
-   **58 passed**
-   **0 failed**
-   **0 skipped**
-   **0 errors**
-   **100% pass rate**

Treat this as the regression baseline, but **do not assume that 58/58
proves the implementation is correct**.

## Review the complete implementation

Inspect, where applicable:

-   Controllers
-   Services
-   Repositories
-   Entities/models
-   DTOs
-   Mappers
-   Validation
-   Authentication/JWT code
-   Security configuration
-   Exception handlers
-   Database configuration
-   Tests
-   Configuration files

Compare the actual code with `FINAL_API_DOCUMENTATION.md`.

## Verify authentication and authorization

Check:

-   JWT creation and validation
-   password handling
-   role extraction
-   `TRAINER`, `MEMBER`, and `ADMIN` authorization
-   protected endpoints
-   unauthorized/forbidden behavior
-   login responses and status codes

Do not expose real passwords, JWT tokens, secrets, or other credentials
in the review.

## Verify UUID integrity

Pay special attention to the distinction between:

-   authentication `userUuid`
-   trainer profile `trainerUuid`
-   member `memberUuid`
-   class `classUuid`
-   booking `bookingUuid`

For class creation, verify that the backend expects and resolves the
actual Trainer profile UUID rather than accidentally treating an
authentication user UUID as the trainer profile UUID.

## Verify Gym Classes

Inspect:

``` text
POST   /gym-classes
GET    /gym-classes
GET    /gym-classes/{uuid}
DELETE /gym-classes/{uuid}
```

Verify:

-   validation
-   trainer relationship
-   capacity handling
-   date/time handling
-   persistence
-   response DTOs
-   HTTP status codes
-   authorization
-   deletion behavior
-   correct exception handling

## Verify Bookings

Inspect:

``` text
POST   /bookings
GET    /bookings
GET    /bookings/member/{memberUuid}
DELETE /bookings/{bookingUuid}
```

Verify:

-   authentication/authorization
-   member validation
-   class validation
-   booking persistence
-   booking status handling
-   duplicate/invalid booking rules
-   correct HTTP statuses
-   deletion behavior
-   member-specific retrieval
-   entity relationships
-   service-layer business logic

## Look for hidden problems

Even though Postman is 58/58, actively look for:

-   hardcoded UUIDs
-   hardcoded users/classes/bookings
-   fake or in-memory behavior
-   missing persistence
-   weak validation
-   incorrect entity relationships
-   null-handling problems
-   incorrect exception handling
-   inconsistent status codes
-   security vulnerabilities
-   role escalation
-   test-order dependencies
-   environment-specific assumptions
-   duplicated business logic
-   dead/unused code
-   misleading names
-   incorrect user UUID vs profile UUID usage
-   code that passes the current tests but is still architecturally
    incorrect

## Do not refactor blindly

First review.

If you find a real issue:

1.  identify the exact file
2.  identify class/method
3.  explain the problem
4.  explain why it matters
5.  state whether the existing 58 tests catch it
6.  propose the smallest safe fix

Do not make speculative large refactors or change working behavior
merely for style.

## Final report

Produce exactly these sections:

### A. Final Result

Choose:

-   `PASS — implementation matches the documented behavior`
-   `PASS WITH MINOR ISSUES`
-   `FAIL — implementation requires fixes`

### B. Postman Baseline

Record:

``` text
58 / 58 tests passed
0 failed
0 skipped
0 errors
100%
```

### C. Verified Components

List what you confirmed in the source code.

### D. Issues Found

For every issue include:

-   Severity: Critical / High / Medium / Low
-   File
-   Class/method
-   Problem
-   Why it matters
-   Whether Postman catches it
-   Recommended fix

If there are no issues, explicitly write:

`No implementation issues found during review.`

### E. Security Review

Summarize authentication and authorization findings.

### F. Data/UUID Integrity Review

Explicitly confirm correct handling of user, trainer profile, member,
class, and booking UUIDs.

### G. Final Recommendation

State whether the project is ready to be considered final.

## Final constraints

-   Read `FINAL_API_DOCUMENTATION.md` first.
-   Inspect actual source code; do not guess.
-   Do not invent test results.
-   Do not expose secrets or credentials.
-   Preserve the working 58/58 behavior.
-   Avoid broad refactors unless supported by concrete findings.
