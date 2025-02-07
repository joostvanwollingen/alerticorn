package nl.vanwollingen.alerticorn.api

/**
 * Interface for notifiers that send messages to different platforms.
 *
 * Implementations of this interface must define a platform name and the logic
 * for sending messages.
 *
 */
interface Notifier {

    /**
     * Retrieves the platform name for this notifier.
     *
     * @return The platform name (e.g., "slack", "discord").
     */
    fun getPlatform(): String

    /**
     * Sends a message to the specified destination.
     *
     * @param message The `AlerticornMessage` to be sent.
     * @param destination The target destination (e.g., a channel ).
     */
    fun send(message: AlerticornMessage, destination: String)
}
