package nl.vanwollingen.alerticorn.discord

import nl.vanwollingen.alerticorn.api.AlerticornMessage
import nl.vanwollingen.alerticorn.api.MessageFailedToSendException
import nl.vanwollingen.alerticorn.api.Notifier
import nl.vanwollingen.alerticorn.http.HttpClient

class DiscordNotifier : Notifier {
    override fun getPlatform() = "discord"

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