package nl.vanwollingen.alerticorn.discord

import nl.vanwollingen.alerticorn.api.AlerticornMessage
import nl.vanwollingen.alerticorn.api.MessageFailedToSendException
import nl.vanwollingen.alerticorn.api.Notifier
import nl.vanwollingen.alerticorn.http.HttpClient

/**
 * A notifier that sends messages to Discord via webhooks.
 *
 * The [DiscordNotifier] implements the [Notifier] interface and is responsible for sending
 * [AlerticornMessage] objects to a specified Discord webhook URL. The message is converted
 * to the format required by Discord and sent using an HTTP POST request.
 */
class DiscordNotifier : Notifier {

    override fun getPlatform() = "discord"

    /**
     * Sends an [AlerticornMessage] to a specified Discord webhook destination.
     *
     * This method converts the [AlerticornMessage] to the required Discord webhook format
     * and calls the webhook using an HTTP POST request. If the response status code is not
     * 204, a [MessageFailedToSendException] is thrown.
     *
     * @param message The [AlerticornMessage] to send.
     * @param destination The URL of the Discord webhook.
     * @throws MessageFailedToSendException If the message fails to send due to an error or invalid response.
     */
    override fun send(message: AlerticornMessage, destination: String) =
        callWebhook(message.toDiscordWebhookMessage().toJson(), destination)

    private fun callWebhook(message: String, destination: String) {
        try {
            val response = HttpClient.post(
                uri = destination,
                body = message,
                headers = mapOf(
                    "Content-Type" to listOf("application/json")
                )
            )

            if (response.statusCode() != 204) {
                throw MessageFailedToSendException(
                    "HTTP ${response.statusCode()} ${
                        response.headers().map()
                    } ${response.body()}"
                )
            }
        } catch (e: Exception) {
            throw MessageFailedToSendException("Failed to send message to Discord: ${e.message}", e)
        }
    }
}