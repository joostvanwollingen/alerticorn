package nl.vanwollingen.alerticorn.teams

import nl.vanwollingen.alerticorn.api.AlerticornMessage
import nl.vanwollingen.alerticorn.api.MessageFailedToSendException
import nl.vanwollingen.alerticorn.api.Notifier
import nl.vanwollingen.alerticorn.http.HttpClient

/**
 * A notifier that sends messages to Teams via Workflow webhooks.
 *
 * The [TeamsNotifier] implements the [Notifier] interface and is responsible for sending
 * [AlerticornMessage] objects to a specified Teams webhook URL. The message is converted
 * to the format required by Teams and sent using an HTTP POST request.
 */
class TeamsNotifier : Notifier {

    /**
     * Sends an [AlerticornMessage] to a specified Teams Workflow webhook destination.
     *
     * This method converts the [AlerticornMessage] to the required Teams webhook format
     * and calls the webhook using an HTTP POST request. If the response status code is not
     * 202, a [MessageFailedToSendException] is thrown.
     *
     * @param message The [AlerticornMessage] to send.
     * @param destination The URL of the Teams webhook.
     * @throws MessageFailedToSendException If the message fails to send due to an error or invalid response.
     */
    override fun send(message: AlerticornMessage, destination: String) =
        callWebhook(message.toTeamsWebHookMessage().toJson(), destination)

    private fun callWebhook(message: String, destination: String) {
        try {
            val response = HttpClient.post(
                destination,
                body = message,
                headers = mapOf("Content-Type" to listOf("application/json"))
            )
            if (response.statusCode() != 202) {
                throw MessageFailedToSendException("${response.statusCode()}: ${response.body()}")
            }
        } catch (e: Exception) {
            throw MessageFailedToSendException("Failed to send message to Teams: ${e.message}", e)
        }
    }

    override fun getPlatform() = "teams"
}