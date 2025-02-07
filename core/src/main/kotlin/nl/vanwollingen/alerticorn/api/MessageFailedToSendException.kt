package nl.vanwollingen.alerticorn.api

/**
 * Exception thrown when a message fails to be sent via a notifier.
 *
 * This exception can be used to indicate issues such as missing platform configurations,
 * unavailable notifiers, or other message delivery failures.
 *
 */
open class MessageFailedToSendException : Exception {

    /**
     * Constructs a [MessageFailedToSendException] with no detail message.
     */
    constructor() : super()

    /**
     * Constructs a [MessageFailedToSendException] with the specified detail message.
     *
     * @param message The detail message explaining the cause of the exception.
     */
    constructor(message: String) : super(message)

    /**
     * Constructs a [MessageFailedToSendException] with the specified detail message and cause.
     *
     * @param message The detail message explaining the cause of the exception.
     * @param cause The cause of the exception.
     */
    constructor(message: String, cause: Throwable) : super(message, cause)

    /**
     * Constructs a [MessageFailedToSendException] with the specified cause.
     *
     * @param cause The cause of the exception.
     */
    constructor(cause: Throwable) : super(cause)
}
