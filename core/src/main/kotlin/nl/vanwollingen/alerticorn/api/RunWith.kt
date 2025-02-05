package nl.vanwollingen.alerticorn.api

object RunWith {
    fun <T> messageOnException(
        platform: String,
        destination: String,
        title: String,
        messageBuilder: (String, Throwable) -> AlerticornMessage,
        block: () -> T,
    ): T = runCatching {
        block()
    }.onFailure { e ->
        NotificationManager.notify(
            platform = platform,
            message = messageBuilder(title, e),
            destination = destination,
        )
    }.getOrThrow()

    fun <T> message(
        platform: String,
        destination: String,
        title: String,
        buildMessage: (String, T) -> AlerticornMessage,
        block: () -> T,
    ): T = run {
        val value = block()
        NotificationManager.notify(
            platform = platform,
            message = buildMessage(title, value),
            destination = destination,
        )
        value
    }
}