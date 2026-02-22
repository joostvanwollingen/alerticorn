# Alerticorn: TestNG & Kotest Implementation Plan

## Status Overview

| Phase | Status | Description |
|-------|--------|-------------|
| Phase 1 | COMPLETE | Core changes (Event enum, utility extraction) |
| Phase 2 | COMPLETE | TestNG module implementation |
| Phase 3 | COMPLETE | Kotest module implementation |
| Phase 4 | COMPLETE | Build config, settings, publishing |

**All 103 tests pass across 7 modules.**

| Module | Tests |
|--------|-------|
| core | 30 |
| junit | 14 |
| testng | 17 |
| kotest | 28 |
| slack-webhook-notifier | 5 |
| discord-notifier | 5 |
| teams-workflow-notifier | 4 |

---

## Phase 1: Core Changes

### 1.1 Add suite-level events to `Event` enum

**File:** `core/src/main/kotlin/nl/vanwollingen/alerticorn/api/Message.kt`

- [x] Add `SUITE_START` to the `Event` enum
- [x] Add `SUITE_COMPLETE` to the `Event` enum
- [x] Remove the TODO comment
- [x] Verify existing core tests still pass

### 1.2 Extract `stringArrayToMap` to core

Both the JUnit `MessageExtension` and the TestNG stub had their own
`stringArrayToMap` implementation. Extracted to core to avoid duplication.

**New file:** `core/src/main/kotlin/nl/vanwollingen/alerticorn/api/StringArrayToMap.kt`

- [x] Create top-level function `stringArrayToMap(stringArray: Array<String>): Map<String, String>` in the `api` package
- [x] Update `junit/MessageExtension.kt` to delegate to the core function
- [x] Write tests for the core `stringArrayToMap` function (`core/.../StringArrayToMapTest.kt`)
- [x] Verify existing JUnit tests still pass

---

## Phase 2: TestNG Module

### 2.1 Build config cleanup

**File:** `testng/build.gradle.kts`

- [x] Remove `implementation(project(":slack-webhook-notifier"))` -- notifiers are user-provided via SPI
- [x] Remove `tasks.test { useTestNG() }` -- root `build.gradle.kts` applies `useJUnitPlatform()`
- [x] Add `testImplementation("org.jetbrains.kotlin:kotlin-test")`
- [x] Add `testImplementation("io.mockk:mockk:1.13.13")`
- [x] Add `testImplementation("org.junit-pioneer:junit-pioneer:2.3.0")`
- [x] Add `testImplementation("com.google.inject:guice:7.0.0")` (needed for mocking `ISuite`)
- [x] Add `publishing` block with artifactId `alerticorn-testng`

### 2.2 Implement `MessageListener`

**File:** `testng/src/main/kotlin/nl/vanwollingen/alerticorn/testng/MessageListener.kt`

Replaced the commented-out stub with a full implementation using
the existing `@Message` annotations from core.

**Interfaces:** `ITestListener` + `ISuiteListener`

**Test-level event mapping:**

| TestNG callback | Alerticorn Event |
|-----------------|-----------------|
| `onTestSuccess(result)` | `Event.PASS` |
| `onTestFailure(result)` | `Event.FAIL` (+ `Event.EXCEPTION` if throwable present) |
| `onTestSkipped(result)` | `Event.SKIP` |
| `onTestFailedButWithinSuccessPercentage(result)` | `Event.FAIL` |

Note: `onTestFailedWithTimeout` is a default method in `ITestListener` that
delegates to `onTestFailure`, so it does not need a separate override.

**Suite-level event mapping:**

| TestNG callback | Alerticorn Event |
|-----------------|-----------------|
| `onStart(suite)` | `Event.SUITE_START` |
| `onFinish(suite)` | `Event.SUITE_COMPLETE` |

**Annotation resolution:**
- Method-level: `result.method.constructorOrMethod.method.getAnnotation(T::class.java)`
- Class-level: `result.testClass.realClass.getAnnotation(T::class.java)`
- Method takes precedence over class (same as JUnit)
- Uses standard `java.lang.reflect` annotation lookup (no JUnit `AnnotationSupport`)

**EXCEPTION event handling -- behavioral difference from JUnit:**
In JUnit, `Event.EXCEPTION` fires in `handleTestExecutionException` (before
outcome) and `Event.FAIL` fires in `testFailed` (after outcome). A test with
`@Message.Events([Event.EXCEPTION, Event.FAIL])` gets TWO notifications in JUnit.
In TestNG there is no pre-outcome exception hook, so both are checked in
`onTestFailure` and only ONE notification is sent. This is intentional --
sending duplicates would be worse.

**Suite event handling:**
On `ISuiteListener.onFinish(suite)`:
1. Collect all distinct test classes from `suite.allMethods`
2. For each class with `@Message` + `@Message.Events` containing `SUITE_COMPLETE`:
   - Build the `AlerticornMessage` from the annotation
   - Enrich the message `details` with suite summary: suite name, total/passed/failed/skipped counts (extracted from `suite.results`)
   - Send via `NotificationManager.notify()`

On `ISuiteListener.onStart(suite)`:
- Same class scanning, looking for `SUITE_START` in events
- No result enrichment (the suite hasn't run yet)

**Registration mechanisms:**
1. SPI auto-detection via `META-INF/services/org.testng.ITestNGListener` (already existed)
2. `@Listeners(MessageListener::class)` on test classes
3. `testng.xml` configuration

**Error handling:** Same as JUnit -- catch `MessageFailedToSendException`, print
stack trace, never interfere with test outcomes.

#### Implementation tasks

- [x] Implement `MessageListener` with `ITestListener` + `ISuiteListener`
- [x] Implement `getAnnotationDetails()` using `java.lang.reflect`
- [x] Implement `handleEvent()` with event filtering logic
- [x] Implement `handleSuiteEvent()` with class scanning and result enrichment
- [x] Implement `notify()` wrapper with error handling
- [x] Use core `stringArrayToMap` for annotation array conversion
- [x] Verify SPI file is correct (`META-INF/services/org.testng.ITestNGListener`)

### 2.3 Tests for `MessageListener`

**File:** `testng/src/test/kotlin/nl/vanwollingen/alerticorn/testng/MessageListenerTest.kt`

Tests use **JUnit 6** with MockK.

- [x] Test: no notification when `@Message` annotation is absent
- [x] Test: `onTestSuccess` sends notification when `Event.PASS` is in events
- [x] Test: `onTestSuccess` does NOT send when `Event.PASS` is not in events
- [x] Test: `onTestFailure` sends notification when `Event.FAIL` is in events
- [x] Test: `onTestFailure` does NOT send when `Event.FAIL` is not in events
- [x] Test: `onTestFailure` sends notification when `Event.EXCEPTION` is in events and throwable is present
- [x] Test: `onTestFailure` does NOT send EXCEPTION when throwable is null
- [x] Test: `onTestSkipped` sends notification when `Event.SKIP` is in events
- [x] Test: `Event.ANY` triggers notification for any event
- [x] Test: no `@Message.Events` annotation triggers notification for all events
- [x] Test: annotation fields (title, body, details, links) are correctly mapped to `AlerticornMessage`
- [x] Test: method-level annotations override class-level annotations
- [x] Test: `@Message.Template` delegates to `MessageProvider`
- [x] Test: `onTestFailedButWithinSuccessPercentage` maps to FAIL event
- [x] Test: suite `onFinish` sends notification for classes with `SUITE_COMPLETE` event
- [x] Test: suite `onStart` sends `SUITE_START` notification
- [x] Test: `MessageFailedToSendException` is caught and does not propagate

---

## Phase 3: Kotest Module

### 3.1 Create module structure

New separate module added to the project.

```
kotest/
  build.gradle.kts
  src/
    main/kotlin/nl/vanwollingen/alerticorn/kotest/
      AlerticornExtension.kt
      AlerticornTestCaseExtension.kt
      AlerticornProjectExtension.kt
      AlerticornDsl.kt
    test/kotlin/nl/vanwollingen/alerticorn/kotest/
      AlerticornExtensionTest.kt
      AlerticornTestCaseExtensionTest.kt
      AlerticornProjectExtensionTest.kt
      AlerticornDslTest.kt
```

- [x] Module structure created
- [x] Added to `settings.gradle.kts`

### 3.2 Build config

**File:** `kotest/build.gradle.kts`

- [x] Dependency on `:core`
- [x] Dependency on `io.kotest:kotest-framework-api:5.9.1`
- [x] Test dependencies: kotlin-test, mockk, junit-pioneer
- [x] Tests run on JUnit Platform (inherited from root)
- [x] `publishing` block with artifactId `alerticorn-kotest`

### 3.3 `AlerticornExtension` (spec-level, Approach A)

**File:** `kotest/src/main/kotlin/nl/vanwollingen/alerticorn/kotest/AlerticornExtension.kt`

**Interfaces:** `AfterTestListener` + `TestCaseExtension`

Reads `@Message` annotations from the Spec class and sends notifications
based on test outcomes.

**Event mapping:**

| Kotest `TestResult` | Alerticorn Event(s) |
|---------------------|-------------------|
| `TestResult.Success` | `Event.PASS` |
| `TestResult.Failure` (assertion failure) | `Event.FAIL` |
| `TestResult.Error` (unexpected exception) | `Event.FAIL` + `Event.EXCEPTION` |
| `TestResult.Ignored` | `Event.SKIP` / `Event.DISABLED` (both checked) |

**Registration:**
- Per-spec: `extensions(AlerticornExtension())` in spec init block
- Global: via `ProjectConfig.extensions()`

#### Implementation tasks

- [x] Implement `AlerticornExtension` with `AfterTestListener` + `TestCaseExtension`
- [x] Implement spec-class annotation resolution
- [x] Implement `AnnotationSpec` method-level annotation support via name-based method lookup
- [x] Implement event mapping from `TestResult` types (companion `mapResultToEvents`)
- [x] Use core `stringArrayToMap` for annotation array conversion

### 3.4 `AlerticornTestCaseExtension` (per-test, Approach B)

**File:** `kotest/src/main/kotlin/nl/vanwollingen/alerticorn/kotest/AlerticornTestCaseExtension.kt`

**Interface:** `TestCaseExtension`

Per-test extension carrying its own message configuration. Used via
`.config(extensions = ...)` on individual test cases.

- [x] Constructor accepts all message config (title, body, details, links, events, platform, channel, template)
- [x] Implement `intercept()` with event mapping and notification
- [x] Template support via `MessageProvider`

### 3.5 `AlerticornProjectExtension` (project-level)

**File:** `kotest/src/main/kotlin/nl/vanwollingen/alerticorn/kotest/AlerticornProjectExtension.kt`

**Interfaces:** `BeforeProjectListener` + `AfterProjectListener` + `AfterTestListener`

- [x] Accumulate test results in `afterAny()` (thread-safe `AtomicInteger` counters)
- [x] Build summary message in `afterProject()` with total/passed/failed/errors/ignored
- [x] Implement `beforeProject()` for `SUITE_START` notification

### 3.6 DSL helpers

**File:** `kotest/src/main/kotlin/nl/vanwollingen/alerticorn/kotest/AlerticornDsl.kt`

- [x] `alerticorn()` builder function returning `AlerticornTestCaseExtension` (works with ALL spec styles via `.config()`)
- [x] `FunSpecRootScope.alerticornTest()` extension function for top-level FunSpec tests
- [x] `FunSpecContainerScope.alerticornTest()` extension function for nested FunSpec context tests

Note: `DescribeSpec` and `StringSpec` wrappers were not implemented because
the universal `.config(extensions = listOf(alerticorn(...)))` approach works
with all spec styles. Only `FunSpec` wrappers were added as the most commonly
used convenience.

### 3.7 Tests for Kotest module

Tests use **JUnit 6** with MockK.

**`AlerticornExtensionTest.kt`** (12 tests):
- [x] No notification when no `@Message` annotation on spec class
- [x] `TestResult.Success` sends when `Event.PASS` is configured
- [x] `TestResult.Success` does NOT send when `Event.PASS` is not configured
- [x] `TestResult.Failure` sends when `Event.FAIL` is configured
- [x] `TestResult.Error` sends for `Event.EXCEPTION`
- [x] `TestResult.Ignored` sends when `Event.SKIP` is configured
- [x] `Event.ANY` triggers for success
- [x] `Event.ANY` triggers for failure
- [x] No `@Message.Events` annotation triggers for all events
- [x] Annotation fields mapped correctly to `AlerticornMessage`
- [x] `@Message.Template` delegates to `MessageProvider`
- [x] `MessageFailedToSendException` is caught
- [x] `mapResultToEvents` returns correct events for each result type

**`AlerticornTestCaseExtensionTest.kt`** (7 tests):
- [x] Sends notification based on configured events
- [x] Does NOT send when event is not configured
- [x] Empty events list means all events trigger
- [x] Constructor params mapped to `AlerticornMessage` correctly
- [x] Template support works
- [x] Original `TestResult` is returned unmodified
- [x] `MessageFailedToSendException` is caught

**`AlerticornProjectExtensionTest.kt`** (5 tests):
- [x] `afterProject()` sends notification with accumulated counts
- [x] `beforeProject()` sends `SUITE_START` notification
- [x] `beforeProject()` does NOT send when `SUITE_START` not in events
- [x] `afterProject()` does NOT send when `SUITE_COMPLETE` not in events
- [x] Counts are accurate after multiple test results

**`AlerticornDslTest.kt`** (3 tests):
- [x] `alerticorn()` builder returns correctly configured extension
- [x] Builder with defaults returns extension
- [x] Builder with template returns extension

---

## Phase 4: Build Config & Integration

### 4.1 Update `settings.gradle.kts`

- [x] Add `include("kotest")` to include the new module

### 4.2 Verify all modules build and tests pass

- [x] `./gradlew :core:test` passes (30 tests)
- [x] `./gradlew :junit:test` passes (14 tests)
- [x] `./gradlew :testng:test` passes (17 tests)
- [x] `./gradlew :kotest:test` passes (28 tests)
- [x] `./gradlew build` succeeds for the full project

---

## Design Decisions & Known Differences

### EXCEPTION event semantics across frameworks

| Framework | When EXCEPTION fires | When FAIL fires | Dual notification? |
|-----------|---------------------|----------------|-------------------|
| JUnit 6 | In `handleTestExecutionException` (before outcome) | In `testFailed` (after outcome) | Yes -- `@Message.Events([EXCEPTION, FAIL])` sends TWO notifications |
| TestNG | In `onTestFailure` (after outcome, if throwable present) | In `onTestFailure` (after outcome) | No -- checked together, sends ONE notification |
| Kotest | In `TestCaseExtension.intercept` (during execution) | In `AfterTestListener.afterTest` (after outcome) | Yes -- same dual-hook model as JUnit |

### Annotation applicability by framework

| Framework | Method-level `@Message` | Class-level `@Message` | DSL config |
|-----------|------------------------|----------------------|------------|
| JUnit 6 | Yes (primary) | Yes (fallback) | N/A |
| TestNG | Yes (primary) | Yes (fallback) | N/A |
| Kotest (AnnotationSpec) | Yes (primary) | Yes (fallback) | N/A |
| Kotest (other specs) | N/A (tests are lambdas) | Yes (primary) | `.config(extensions=...)` or `alerticornTest()` |

### Suite/project events

| Framework | Mechanism | Results available? |
|-----------|-----------|-------------------|
| TestNG | `ISuiteListener.onFinish(suite)` | Yes, via `suite.results` |
| Kotest | `AfterProjectListener.afterProject()` | No -- accumulated via `AfterTestListener` with `AtomicInteger` counters |

### `Event.DISABLED` vs `Event.SKIP`

- JUnit 6: `DISABLED` = `@Disabled` annotation; `SKIP` is not used by JUnit (but is in the enum)
- TestNG: `SKIP` = `@Test(enabled=false)`, dependency failure, or `throw SkipException`; `DISABLED` is not used
- Kotest: `Ignored` = `xtest`, `config(enabled=false)`, etc.; both `SKIP` and `DISABLED` are checked

### TestNG `onTestFailedWithTimeout`

This is a default method in `ITestListener` (since TestNG 7.0) that delegates
to `onTestFailure`. No separate override is needed -- our `onTestFailure`
implementation handles it automatically.

---

## Files Changed/Created

### Core module
- `core/src/main/kotlin/nl/vanwollingen/alerticorn/api/Message.kt` -- Added `SUITE_START`, `SUITE_COMPLETE` to `Event` enum
- `core/src/main/kotlin/nl/vanwollingen/alerticorn/api/StringArrayToMap.kt` -- **NEW** -- Extracted utility function
- `core/src/test/kotlin/nl/vanwollingen/alerticorn/api/StringArrayToMapTest.kt` -- **NEW** -- 4 tests

### JUnit module
- `junit/src/main/kotlin/nl.vanwollingen.alerticorn.junit/MessageExtension.kt` -- Delegates `stringArrayToMap` to core

### TestNG module
- `testng/build.gradle.kts` -- Cleaned up dependencies, added publishing
- `testng/src/main/kotlin/nl/vanwollingen/alerticorn/testng/MessageListener.kt` -- **REWRITTEN** -- Full `ITestListener` + `ISuiteListener` implementation
- `testng/src/test/kotlin/nl/vanwollingen/alerticorn/testng/MessageListenerTest.kt` -- **NEW** -- 17 tests

### Kotest module (all new)
- `kotest/build.gradle.kts` -- **NEW**
- `kotest/src/main/kotlin/nl/vanwollingen/alerticorn/kotest/AlerticornExtension.kt` -- **NEW**
- `kotest/src/main/kotlin/nl/vanwollingen/alerticorn/kotest/AlerticornTestCaseExtension.kt` -- **NEW**
- `kotest/src/main/kotlin/nl/vanwollingen/alerticorn/kotest/AlerticornProjectExtension.kt` -- **NEW**
- `kotest/src/main/kotlin/nl/vanwollingen/alerticorn/kotest/AlerticornDsl.kt` -- **NEW**
- `kotest/src/test/kotlin/nl/vanwollingen/alerticorn/kotest/AlerticornExtensionTest.kt` -- **NEW** -- 13 tests
- `kotest/src/test/kotlin/nl/vanwollingen/alerticorn/kotest/AlerticornTestCaseExtensionTest.kt` -- **NEW** -- 7 tests
- `kotest/src/test/kotlin/nl/vanwollingen/alerticorn/kotest/AlerticornProjectExtensionTest.kt` -- **NEW** -- 5 tests
- `kotest/src/test/kotlin/nl/vanwollingen/alerticorn/kotest/AlerticornDslTest.kt` -- **NEW** -- 3 tests

### Project config
- `settings.gradle.kts` -- Added `include("kotest")`
