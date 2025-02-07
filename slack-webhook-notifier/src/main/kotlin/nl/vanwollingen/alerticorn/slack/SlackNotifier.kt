package nl.vanwollingen.alerticorn.slack

import nl.vanwollingen.alerticorn.api.AlerticornMessage
import nl.vanwollingen.alerticorn.api.MessageFailedToSendException
import nl.vanwollingen.alerticorn.api.Notifier
import nl.vanwollingen.alerticorn.http.HttpClient

/**
 * A notifier that sends messages to Slack via webhooks.
 *
 * The [SlackNotifier] implements the [Notifier] interface and is responsible for sending
 * [AlerticornMessage] objects to a specified Slack webhook URL. The message is converted
 * to the format required by Slack and sent using an HTTP POST request.
 */
class SlackNotifier : Notifier {

    /**
     * Sends an [AlerticornMessage] to a specified Slack webhook destination.
     *
     * This method converts the [AlerticornMessage] to the required Slack webhook format
     * and calls the webhook using an HTTP POST request. If the response status code is not
     * 200, a [MessageFailedToSendException] is thrown.
     *
     * @param message The [AlerticornMessage] to send.
     * @param destination The URL of the Slack webhook.
     * @throws MessageFailedToSendException If the message fails to send due to an error or invalid response.
     */
    override fun send(message: AlerticornMessage, destination: String) =
        callWebhook(message.toSlackWebHookMessage().toJson(), destination)

    private fun callWebhook(message: String, destination: String) {
        try {
            val response = HttpClient.post(destination, body = message)
            if (response.statusCode() != 200) {
                throw MessageFailedToSendException("${response.statusCode()}: ${response.body()}")
            }
        } catch (e: Exception) {
            throw MessageFailedToSendException("Failed to send message to Slack: ${e.message}", e)
        }
    }

    override fun getPlatform() = "slack"
}