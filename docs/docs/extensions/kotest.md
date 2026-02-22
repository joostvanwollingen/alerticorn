# Kotest

The Kotest extension adds support for Alerticorn notifications in Kotest test suites.

Because Kotest tests are typically lambdas inside a DSL (not annotated methods), the Kotest module
offers two approaches:

- **Approach A: Spec-level annotations** -- Place `@Message` annotations on your Spec class. All tests in that spec are covered.
- **Approach B: Per-test DSL configuration** -- Use `.config(extensions = ...)` or the `alerticornTest()` helper for individual test granularity.

## Approach A: Spec-level extension

Register `AlerticornExtension` in your spec and annotate the Spec class:

```kotlin
@Message(title = "Payment test failed")
@Message.Events([Event.FAIL])
@Message.Platform("slack")
@Message.Channel("alerts")
class PaymentSpec : FunSpec({
    extensions(AlerticornExtension())

    test("process payment") {
        // If this test fails, a notification is sent
    }

    test("refund payment") {
        // Same notification settings apply to all tests in this spec
    }
})
```

You can also register the extension globally via `ProjectConfig`:

```kotlin
class ProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(AlerticornExtension())
}
```

## Approach B: Per-test DSL configuration

### Using `.config(extensions = ...)`

The `alerticorn()` builder function works with **all** Kotest spec styles:

```kotlin
class PaymentSpec : FunSpec({
    test("critical payment test").config(
        extensions = listOf(
            alerticorn(
                title = "Critical payment test failed!",
                events = listOf(Event.FAIL),
                platform = "slack",
                channel = "critical-alerts"
            )
        )
    ) {
        // test body
    }
})
```

### Using `alerticornTest()` (FunSpec convenience)

For `FunSpec`, a convenience wrapper is available:

```kotlin
class PaymentSpec : FunSpec({
    alerticornTest(
        name = "critical payment test",
        title = "Critical payment test failed!",
        events = listOf(Event.FAIL),
        platform = "slack",
        channel = "critical-alerts"
    ) {
        // test body
    }
})
```

This also works inside `context` blocks:

```kotlin
class PaymentSpec : FunSpec({
    context("payments") {
        alerticornTest("process payment", title = "Payment failed!", events = listOf(Event.FAIL)) {
            // test body
        }
    }
})
```

## Project-level notifications

Use `AlerticornProjectExtension` to send notifications when the entire test project starts or finishes:

```kotlin
class ProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(
        AlerticornProjectExtension(
            title = "Test suite finished",
            platform = "slack",
            channel = "ci-results",
            events = listOf(Event.SUITE_COMPLETE)
        )
    )
}
```

The `SUITE_COMPLETE` notification includes summary details: `total`, `passed`, `failed`, `errors`, and `ignored` counts.

## Supported events

| Event | When it fires |
|-------|--------------|
| `Event.PASS` | `TestResult.Success` |
| `Event.FAIL` | `TestResult.Failure` (assertion error) or `TestResult.Error` (unexpected exception) |
| `Event.EXCEPTION` | `TestResult.Error` only (unexpected exception, not assertion failure) |
| `Event.SKIP` | `TestResult.Ignored` |
| `Event.DISABLED` | `TestResult.Ignored` (alias for `SKIP` in Kotest) |
| `Event.ANY` | Any of the above |
| `Event.SUITE_START` | Before any test runs (via `AlerticornProjectExtension`) |
| `Event.SUITE_COMPLETE` | After all tests finish (via `AlerticornProjectExtension`) |

## FAQ

### Can I use `@Message` annotations on individual test methods?

Only with `AnnotationSpec`. In `AnnotationSpec`, tests are actual methods (like JUnit), so
method-level `@Message` annotations work and take precedence over class-level annotations.

For all other spec styles (FunSpec, DescribeSpec, StringSpec, etc.), tests are lambdas and
cannot carry annotations. Use `.config(extensions = ...)` or `alerticornTest()` instead.

### How does `Event.EXCEPTION` differ from `Event.FAIL` in Kotest?

Kotest distinguishes between assertion failures (`TestResult.Failure`) and unexpected exceptions
(`TestResult.Error`). `Event.FAIL` fires for both, while `Event.EXCEPTION` fires only for
`TestResult.Error`. This gives you finer control -- you can be notified only when unexpected
exceptions occur, not routine assertion failures.
