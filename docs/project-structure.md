# Project structure

One shared module holds the product code. Features are packages inside it. A new module is added only for the cases listed at the end.

The Android app and the iOS app use this module. They draw the screens. They do not own the rules.

## Layout

```text
shared/src
  feature/
    login/            account, role, employee id
    classes/          class list, detail, request, public or private
    schedule/         days, time, dates through the end of the year, cancel one date
    invite/           invite, join request, roster
    attendance/       class choice, scan, present, absent
  core/
    database/         Room
    network/          Supabase
    cache/            offline copy and the upload queue
```

`classes` is the Class feature. The folder is not named `class` because that word is reserved in Kotlin.

## What each area owns

A feature package holds the types and the rules its tests cover. Domain stays there. `core` stores and sends those types. It does not decide the rules.

A feature calls an interface. The app connects that interface to the `core` implementation when it starts. Feature tests use a fake of the interface.

`cache` is the piece that keeps a scan on the phone and uploads it when a connection returns. Attendance depends on that interface.

## Another module

Create another module in this same project only when one of these is true:

1. A dependency the rest of the app should not compile.
2. The shared test run is too slow, and that feature’s tests need to run alone.
3. The code is a platform detail several features use, and those features should depend on an interface instead of that stack.
4. A second app in this project needs the code and the mobile app does not.
5. The feature is built or shipped on a different schedule from the app.

A new screen or a new product feature stays a package under `feature/`.
