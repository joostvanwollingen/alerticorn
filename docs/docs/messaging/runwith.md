---
sidebar_position: 2
---
# Run With

## RunWith.message
`RunWith.message` accepts a title and a function that is passed the result of the block to construct a message.

This code:

```kotlin title="Example"
RunWith.message(
    platform = "slack",
    destination = "https://hooks.slack.com/services/<your>/<slack>/<webhook>",
    title = "Hello!",
    buildMessage = { message, value -> AlerticornMessage(message, "the value was $value") },
    block = { "World" }
)
```

Would produce:

![img.png](/img/runWithMessage.png)

## RunWith.messageOnException
`messageOnException` works the same, but only sends the message if an exception happens while running the code block.

```kotlin title="Example"
 RunWith.messageOnException(
    platform = "slack",
    destination = "https://hooks.slack.com/services/<your>/<slack>/<webhook>",
    title = "An urgent message from the chief engineer",
    messageBuilder = { message, value -> AlerticornMessage(message, "the error was $value") },
    block = {
        throw Error("the flux capacitor overloaded")
    }
)
```

![img.png](/img/runWithMessageOnException.png)