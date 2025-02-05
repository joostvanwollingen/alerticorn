---
sidebar_position: 1
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Annotations

To use annotations you must use an extension for your test framework as well. The extension is called by your test
framework
when interesting events happen and takes care of parsing the annotations into the message you want to sent.

:::warning[Extension loaded?!]

This page assumes you have the appropriate extension added to your classpath and loaded by your test framework. If not,
go to [Extensions](/category/extensions).

:::

## @Message

This is the main annotation.

| Attribute | Type            | Default Value | Description                             |
|-----------|-----------------|---------------|-----------------------------------------|
| `title`   | `String`        | (Required)    | The title of the message.               |
| `body`    | `String`        | `""`          | The content of the message.             |
| `details` | `Array<String>` | `[]`          | Additional details in key-value format. |
| `links`   | `Array<String>` | `[]`          | List of related links.                  |

`details` and `links` expect an array with an even number of items, alternating `key` and `value`.

### Examples

<Tabs>
<TabItem value="Title only">

```kotlin
@Message(title = "The flux capacitor failed")
@Test
fun `title only test`() {
    //...
}
```

</TabItem>
<TabItem value="With body">

```kotlin
@Message(
    title = "The flux capacitor failed",
    body = "We should reboot the system."
)
@Test
fun `title and body test`() {
    //...
}
```

</TabItem>
<TabItem value="With details and links">

```kotlin
@Message(
    title = "The flux capacitor failed",
    body = "We should reboot the system.",
    links = ["Homepage", "https://www.google.com"],
    details = ["Team", "Red"]
)
@Test
fun `flux capacitor test`() {
    //...
}
```

</TabItem>
</Tabs>

## @Message.Channel

If you don't configure a default channel, Alerticorn requires that you specify a channel name. The value can be a
webhook
url, or the name of the channel.

When using the name of the channel it tries to resolve the channel name to a webhook url, via
the [environment variables configuration](/configuration).

The example below expects these two environment variables:

- `AC_SLACK_CHANNEL_GENERAL="https://hooks.slack.com/...`
- `AC_SLACK_CHANNEL_ANOTHER_CHANNEL="https://hooks.slack.com/...`

:::note

Annotations at the method level always take precedence over class level annotations.

:::

```kotlin title="Example of overriding the channel at method level"
@Message.Channel("general")
class MyTest {

    fun GeneralTest() {
        ...
    } //this sends to general

    @Message.Channel("another_channel")
    fun SpecificTest() {
        ...
    } //this send to another_channel
}
```

## @Message.Platform

Alerticorn will try to send the message to the [default platform](/configuration#default-platform), or if not
configured, to the first notifier it detects.

If there are [notifiers](/category/notifiers) on the classpath for multiple platforms, the annotation can be used to
override the default platform for a specific class or method.

```kotlin title="Example of overriding the platform at method level"
@Message.Platform("slack")
class MyTest {

    fun GeneralTest() {
        //this sends to general
    } 

    @Message.Platform("teams")
    fun SpecificTest() {
        //this sends to another_channel
    } 
}
```

## @Message.Template

In some cases you may feel limited by the possibilities of the [Message](#message) annotation. You can create template functions that construct an AlerticornMessage.

```kotlin title="Example of using a template to build a message"
    @Test
    @Message(title = "Template test")
    @Message.Template("myFavTemplate")
    fun testt() {
        throw Error("something failed")
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup(): Unit {
            MessageProvider.store("myFavTemplate") { title, throwable ->
                AlerticornMessage(
                    title = "The title was $title",
                    body = "This went wrong: ${throwable?.message}",
                    details = mapOf("fun" to "times"),
                    links = mapOf("google" to "https://www.google.com"),
                    throwable,
                )
            }
        }
    }
```

## @Message.Events

If you do not want to send messages for any test event, but only in restricted cases, you can use @Message.Event.
You can specify one or more of: ANY, EXCEPTION, FAIL, PASS, SKIP, ABORTED, DISABLED.

```kotlin title="Example to send a message only when the test fails"
    @Test
    @Message(title = "The test")
    @Message.Events([Event.FAIL])
    fun theTest() {
        assertEquals(true, false)
    }
```

:::tip ANY and EXCEPTION

When using a test framework extension, a failed test is also considered an exception. You will be notified for any failed test when using EXCEPTION.

ANY encompasses all events and is considered equivalent to not using the Message.Events annotation (ANY is the default).

:::