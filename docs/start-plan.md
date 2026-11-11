# Start plan

How implementation starts once the KMP project is ready. Each step is one red-green-refactor cycle from `docs/build-preferences.md`. The behavior comes from `docs/mvp-requirements.md`. You review after each step before the next one begins.

## Where the first code lives

The first production code is in the shared module. It is plain Kotlin. It does not draw a screen, call Supabase, or read the phone clock.

A unit test in the shared module sets up a catalog of classes, asks it one question, and asserts the answer. When that test fails, the smallest production type is added to make it pass.

Android and iOS screens come after these rules are green. A screen shows the result of a shared type. It does not invent a rule the shared test does not already cover.

## Before the first cycle

The project builds, and one shared unit test runs and fails on purpose. That proves the test runner, not a product rule. After that, product cycles begin.

## Step 1. Class list

**Test.** A guest asks for the public classes. The result contains a class marked public, with its name, coach name, days, and time. A class marked private is absent from that result.

**Production.** A class record and a query that returns only public classes.

## Step 2. Class detail

**Test.** Opening one public class returns the same name, coach, days, and time. The result has no roster, no attendance, and no QR code.

**Production.** The detail is that same public class. Nothing else is added to the type.

## Step 3. Guest request

**Test.** A guest requests that class. The class then shows pending and awaiting approval. The request action is no longer available. A second request does not create a second pending request.

**Production.** One join request tied to that class and that new student, with a pending status.

The test stops at the request. Registration fields and the shake gesture stay on the screen that comes later.

## Step 4. Signed-in student request

**Test.** A student who already has an account requests a public class. One pending request exists immediately.

**Production.** The same request path as step 3, started from an existing student instead of a new account.

## Step 5. Coach has no request

**Test.** A coach opens the same public class. The result has no request action.

**Production.** The detail reports that a coach cannot request.

## Step 6. Request again after a decline

**Test.** The coach declines the request. The student can request that class again, and one new pending request exists.

**Production.** A declined request leaves the student off the class, and a later request creates a new pending one.

Stop here. This is the Class behavior that can be proven without a phone. The next instruction decides whether the following cycle is the schedule or the list screen.

## After these steps

**Schedule.** A failing test for a saved class creating every matching date from the save date through the coach’s end date, and for a class with no end date staying open past 31 December. Then an every-day class, and cancelling one date without removing the other dates. Deleting or cancelling the whole class is what stops an open class.

**Invite.** A failing test for a coach searching an employee ID, and for approving one request onto that class roster.

**Screens.** Compose and SwiftUI show the shared results. A screen test, or a preview check you review, follows the shared test for that screen. The screen does not gain a button the shared rule does not allow.

**Supabase.** A repository stands in for the network in the tests above. The real Supabase calls are written only after those tests describe the data the app sends and receives.
