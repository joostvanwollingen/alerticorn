package nl.vanwollingen.alerticorn.slack

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import nl.vanwollingen.alerticorn.api.AlerticornMessage
import nl.vanwollingen.alerticorn.api.MessageFailedToSendException
import nl.vanwollingen.alerticorn.http.HttpClient
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.net.http.HttpResponse

class SlackNotifierTest {

    @Test
    fun `it should notify`() {
        val response: HttpResponse<String> = mockResponse()

        val (acMessage, json) = buildMessageBodyFor("Hello World!")
        val url = "https://slack.com/webhook"

        mockkObject(HttpClient) {
            every { HttpClient.post(url, body = json) } returns response

            val slackNotifier = SlackNotifier()
            slackNotifier.send(acMessage, url)

            verify(exactly = 1) { HttpClient.post(url, body = json) }
        }
    }

    @Test
    fun `it should throw MessageFailedToSendException on status code != 200`() {
        val response: HttpResponse<String> = mockResponse(403)
        val (acMessage, json) = buildMessageBodyFor("Hello World!")
        val url = "https://slack.com/webhook"

        mockkObject(HttpClient) {
            every {
                HttpClient.post(
                    url,
                    body = json
                )
            } returns response

            val slackNotifier = SlackNotifier()
            assertThrows<MessageFailedToSendException> { slackNotifier.send(acMessage, url) }

            verify(exactly = 1) { HttpClient.post(url, body = json) }
        }
    }

    @Test
    fun `it should throw exceptions on failure to send`() {
        val (acMessage, json) = buildMessageBodyFor("Hello World!")
        val url = "https://slack.com/webhook"

        mockkObject(HttpClient) {
            every {
                HttpClient.post(
                    url,
                    body = json
                )
            } throws IOException("err")

            val slackNotifier = SlackNotifier()
            assertThrows<MessageFailedToSendException> { slackNotifier.send(acMessage, url) }

            verify(exactly = 1) { HttpClient.post(url, body = json) }
        }
    }

    private fun mockResponse(status: Int = 200, body: String? = "OK"): HttpResponse<String> {
        val response: HttpResponse<String> = mockk()
        every { response.statusCode() } returns status
        every { response.body() } returns body
        return response
    }

    private fun buildMessageBodyFor(message: String): Pair<AlerticornMessage, String> {
        val acMessage = AlerticornMessage(
            title = message,
        )
        return Pair(acMessage, acMessage.toSlackWebHookMessage().toJson())
    }
}