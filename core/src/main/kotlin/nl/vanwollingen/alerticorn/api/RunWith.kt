package nl.vanwollingen.alerticorn.api

/**
 * A utility object that provides methods for executing code blocks with notifications
 * sent based on the outcome of those blocks. This can be useful for logging or alerting
 * when certain actions are performed, such as handling exceptions.
 */
object RunWith {

    /**
     * Executes a block of code and sends a notification if an exception is thrown.
     *
     * If the block of code throws an exception, the provided `messageBuilder` function is used
     * to create a message based on the exception and the title, which is then sent to the specified
     * platform and destination using [NotificationManager.notify].
     *
     * @param platform The platform identifier (e.g., "slack", "teams").
     * @param destination The destination for the notification (e.g., channel).
     * @param title The title of the notification.
     * @param messageBuilder A function that builds an [AlerticornMessage] from the title and the exception.
     * @param block The block of code to execute.
     * @return The result of the block execution, or throws the exception if the block fails.
     */
    @JvmStatic
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

    /**
     * Executes a block of code and sends a notification upon successful completion.
     *
     * The result of the block is passed to the provided `buildMessage` function to create
     * a notification message, which is then sent to the specified platform and destination
     * using [NotificationManager.notify].
     *
     * @param platform The platform identifier (e.g., "slack", "teams").
     * @param destination The destination for the notification (e.g., channel).
     * @param title The title of the notification.
     * @param buildMessage A function that builds an [AlerticornMessage] from the title and the result of the block.
     * @param block The block of code to execute.
     * @return The result of the block execution.
     */
    @JvmStatic
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