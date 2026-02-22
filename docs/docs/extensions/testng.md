# TestNG

The TestNG listener adds support to use the [@Message annotation](/messaging/annotations/) on your test classes and
methods.

## Registering the listener with TestNG

In order to integrate Alerticorn with TestNG, the `MessageListener` must be registered as a
[TestNG Listener](https://testng.org/#_testng_listeners).

### Using `@Listeners` annotation

The simplest method is to use the `@Listeners` annotation from TestNG:

```kotlin title="Example of registering the listener"
@Listeners(MessageListener::class)
class MyTest {
    //...
}
```

### Automatic discovery via SPI

The `alerticorn-testng` module ships with a `META-INF/services/org.testng.ITestNGListener` file,
so TestNG will automatically discover and register the listener if the jar is on the classpath.

### Via `testng.xml`

You can also register the listener in your `testng.xml` configuration:

```xml
<suite name="My Suite">
    <listeners>
        <listener class-name="nl.vanwollingen.alerticorn.testng.MessageListener"/>
    </listeners>
    <!-- ... -->
</suite>
```

## Supported events

The TestNG listener supports the following events:

| Event | When it fires |
|-------|--------------|
| `Event.PASS` | Test passes (`onTestSuccess`) |
| `Event.FAIL` | Test fails (`onTestFailure`) |
| `Event.EXCEPTION` | Test fails with a throwable (fires alongside `FAIL` in `onTestFailure`) |
| `Event.SKIP` | Test is skipped (`onTestSkipped`) |
| `Event.ANY` | Any of the above events |
| `Event.SUITE_START` | Suite starts (`onStart`) |
| `Event.SUITE_COMPLETE` | Suite finishes (`onFinish`) |

## Suite-level notifications

You can receive notifications when a test suite starts or completes by placing
`@Message` annotations with suite events on your test class:

```kotlin
@Message(title = "Test suite completed")
@Message.Events([Event.SUITE_COMPLETE])
@Message.Platform("slack")
@Message.Channel("ci-results")
class MyTest {

    @Test
    fun myTest() {
        // ...
    }
}
```

When the suite completes, the notification message will be enriched with suite
summary details: `suite` (name), `total`, `passed`, `failed`, and `skipped` counts.

## FAQ

### How does `Event.EXCEPTION` work in TestNG vs JUnit?

In JUnit, `Event.EXCEPTION` fires *before* the test is marked as failed (via `TestExecutionExceptionHandler`),
and `Event.FAIL` fires *after*. This means a test annotated with both events gets two notifications.

In TestNG, there is no pre-outcome exception hook. Both `Event.EXCEPTION` and `Event.FAIL` are
checked in `onTestFailure`, and only **one** notification is sent. `Event.EXCEPTION` only triggers
when a throwable is present on the test result.
