# Build and code preferences

Rules for how this product is built. Add the next preference as the next numbered section.

## 1. Test-driven development

Production code starts from a failing test. The style is Kent Beck’s red-green-refactor cycle and Robert C. Martin’s three laws of TDD.

**Red.** Write one test for the next behavior. Run it. It must fail. A test that does not compile counts as a failure. Do not write more of the test than is needed to fail.

**Green.** Write only the production code that makes that test pass. Do not add behavior the test does not ask for.

**Refactor.** With the tests green, clean the code. Names, duplication, and structure. The tests stay green. Do not add behavior during refactor.

The three laws:

1. No production code unless it makes a failing test pass.
2. No more of a test than is sufficient to fail.
3. No more production code than is sufficient to pass that one failing test.

A feature is a sequence of these cycles. Each cycle is one behavior from `docs/mvp-requirements.md`. The test names the behavior. The production code is the smallest implementation that satisfies it.

Tests are independent. One test does not rely on another test having run. A test sets up its own state, acts once, and asserts the outcome.

## 2. Approve means commit, then continue

When the review is **Approve**, commit that change, then start the next cycle immediately. Do not wait for a separate instruction to begin the next task.
