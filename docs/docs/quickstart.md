---
sidebar_position: 1
---

import Dependencies from './_dependencies.mdx';

# Quickstart

### Dependencies

<Dependencies />

### Add Annotations

There's two main ways to use Alerticorn: calling `RunWith` directly from your code, or by using Message annotations in
combination with the extension intended for your test framework.

#### Integrating with JUnit

1. Annotate your JUnit test class with the extension

```kotlin
@ExtendWith(MessageExtension::class)
class MyTest {
    ...
}
```

2. Annotate your JUnit test class or method with @Message.

```kotlin
@Message(title = "The flux capacitator failed")
@Test
fun `flux capacitator test`() {
    ...
}
```

### Configuration
Set environment variables for the default channel and it's webhook.

It's recommended to not commit your webhooks in code, but rather get them from the environment. Alerticorn will attempt
to transform channel names into a webhook url, and fallback to using the raw channel value if no webhook is present in
the environment variables.

```shell
AC_DEFAULT_CHANNEL="general"
AC_SLACK_CHANNEL_GENERAL="https://hooks.slack.com/services/.../..."
```

### Run your tests as per usual
You can now launch your tests and messages should start to arrive.