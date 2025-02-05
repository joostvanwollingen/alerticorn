package nl.vanwollingen.alerticorn.slack

import nl.vanwollingen.alerticorn.api.AlerticornMessage
import nl.vanwollingen.alerticorn.api.MessageFailedToSendException
import nl.vanwollingen.alerticorn.api.Notifier
import nl.vanwollingen.alerticorn.http.HttpClient

class SlackNotifier : Notifier {

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