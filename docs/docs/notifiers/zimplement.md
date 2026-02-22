---
sidebar_label: Create Your Own
---

# Create Your Own Notifier

Alerticorn uses Java's [ServiceLoader](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/ServiceLoader.html) mechanism to discover notifiers at runtime. To create your own notifier, you need to implement the `Notifier` interface, convert `AlerticornMessage` into your platform's message format, and register your implementation so Alerticorn can find it.

This guide walks through building a notifier from scratch using a fictional "Acme Chat" platform as an example.

## Prerequisites

Your project needs a dependency on `alerticorn-core`:

```kotlin
dependencies {
    implementation("nl.vanwollingen.alerticorn:alerticorn-core:0.2")
}
```

## Step 1: Implement the Notifier interface

The `Notifier` interface has two methods:

| Method | Purpose |
|---|---|
| `getPlatform()` | Returns the platform name (e.g. `"acme"`). This is the identifier used to route messages to your notifier. |
| `send(message, destination)` | Sends an `AlerticornMessage` to the given destination (typically a webhook URL). |

```kotlin title="AcmeNotifier.kt"
package com.example.alerticorn.acme

import nl.vanwollingen.alerticorn.api.AlerticornMessage
import nl.vanwollingen.alerticorn.api.MessageFailedToSendException
import nl.vanwollingen.alerticorn.api.Notifier
import nl.vanwollingen.alerticorn.http.HttpClient

class AcmeNotifier : Notifier {

    override fun getPlatform() = "acme"

    override fun send(message: AlerticornMessage, destination: String) {
        val body = message.toAcmeJson()
        try {
            val response = HttpClient.post(
                uri = destination,
                headers = mapOf("Content-Type" to listOf("application/json")),
                body = body
            )
            if (response.statusCode() !in 200..299) {
                throw MessageFailedToSendException(
                    "${response.statusCode()}: ${response.body()}"
                )
            }
        } catch (e: Exception) {
            throw MessageFailedToSendException(
                "Failed to send message to Acme: ${e.message}", e
            )
        }
    }
}
```

A few things to note:

- **Platform name** &mdash; The string returned by `getPlatform()` is case-insensitive. It is used to match against `@Message.Platform("acme")` annotations and the `AC_DEFAULT_PLATFORM` environment variable.
- **HttpClient** &mdash; Alerticorn ships an `HttpClient` utility in core that you can use, but you are free to use any HTTP client.
- **Error handling** &mdash; Wrap failures in `MessageFailedToSendException` so that Alerticorn and its test framework extensions can handle them consistently.

## Step 2: Convert AlerticornMessage to your platform's format

The `AlerticornMessage` contains the following fields that you need to map to your platform's message format:

| Field | Type | Description |
|---|---|---|
| `title` | `String` | The message title |
| `body` | `String?` | Optional message body |
| `details` | `Map<String, String>?` | Key-value pairs with additional context |
| `links` | `Map<String, String>?` | Key-value pairs of link labels to URLs |
| `throwable` | `Throwable?` | Optional exception that triggered the message |

Create a conversion function that maps these fields into whatever JSON (or other format) your platform expects:

```kotlin title="AcmeMessage.kt"
package com.example.alerticorn.acme

import nl.vanwollingen.alerticorn.api.AlerticornMessage

fun AlerticornMessage.toAcmeJson(): String {
    val sb = StringBuilder()
    sb.append("""{"text": "$title"""")

    body?.let { sb.append(""", "description": "$it"""") }

    if (!details.isNullOrEmpty()) {
        val formatted = details!!.entries.joinToString("\\n") { "${it.key}: ${it.value}" }
        sb.append(""", "details": "$formatted"""")
    }

    if (!links.isNullOrEmpty()) {
        val formatted = links!!.entries.joinToString(", ") { """"${it.key}": "${it.value}"""" }
        sb.append(""", "links": {$formatted}""")
    }

    throwable?.let {
        sb.append(""", "error": "${it.message}"""")
    }

    sb.append("}")
    return sb.toString()
}
```

:::tip

Look at the existing notifier implementations for reference on how to structure the message conversion. The Slack notifier uses attachments with fields, Discord uses embeds, and Teams uses Adaptive Cards. Pick whatever format your target platform supports.

:::

## Step 3: Register with ServiceLoader

Alerticorn discovers notifiers through Java's `ServiceLoader`. Create a file at the following path in your resources directory:

```
src/main/resources/META-INF/services/nl.vanwollingen.alerticorn.api.Notifier
```

The file contents should be the fully qualified class name of your notifier:

```text title="META-INF/services/nl.vanwollingen.alerticorn.api.Notifier"
com.example.alerticorn.acme.AcmeNotifier
```

This is what allows `NotificationManager` to find your notifier automatically when it's on the classpath.

## Step 4 (optional): Add a shorthand annotation

The built-in notifiers provide shorthand annotations like `@Slack`, `@Discord`, and `@Teams` as shortcuts for `@Message.Platform("...")`. You can do the same for your notifier:

```kotlin title="Acme.kt"
package com.example.alerticorn.acme

import nl.vanwollingen.alerticorn.api.Message

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Message.Platform("acme")
annotation class Acme
```

This lets users write:

```kotlin
@Acme
@Message(title = "Something happened")
@Test
fun myTest() {
    // ...
}
```

instead of:

```kotlin
@Message.Platform("acme")
@Message(title = "Something happened")
@Test
fun myTest() {
    // ...
}
```

## Configuration

Once the notifier is on the classpath, users configure it the same way as any other notifier:

| Environment variable | Purpose |
|---|---|
| `AC_DEFAULT_PLATFORM=acme` | Makes your notifier the default platform |
| `AC_DEFAULT_CHANNEL=mychannel` | Sets the default channel name |
| `AC_ACME_CHANNEL_MYCHANNEL=https://...` | Maps the channel name `mychannel` to a webhook URL |

The channel resolution works through `ConfigurationManager`: when a message is sent to channel `mychannel` on platform `acme`, Alerticorn looks up `AC_ACME_CHANNEL_MYCHANNEL` to find the actual webhook URL.

## Using it

After implementing the notifier and adding it to the classpath, it works automatically with all Alerticorn test framework extensions (JUnit, TestNG, Kotest) and with direct programmatic use:

```kotlin
NotificationManager.notify(
    platform = "acme",
    message = AlerticornMessage(title = "Build passed"),
    destination = "mychannel"
)
```

## Summary

A complete custom notifier consists of three files:

```
src/main/kotlin/com/example/alerticorn/acme/
    AcmeNotifier.kt           // Notifier implementation
    AcmeMessage.kt             // AlerticornMessage conversion
    Acme.kt                    // (optional) Shorthand annotation

src/main/resources/
    META-INF/services/
        nl.vanwollingen.alerticorn.api.Notifier   // ServiceLoader registration
```
