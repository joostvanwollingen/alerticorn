package nl.vanwollingen.alerticorn.teams

import io.mockk.*
import nl.vanwollingen.alerticorn.api.AlerticornMessage
import nl.vanwollingen.alerticorn.api.MessageFailedToSendException
import nl.vanwollingen.alerticorn.http.HttpClient
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.net.http.HttpResponse

class TeamsNotifierTest {

    @Test
    fun `it should notify`() {
        val response: HttpResponse<String> = mockResponse()

        val (acMessage, json) = buildMessageBodyFor("Hello World!")
        val url = "https://1234jkl.webhook.office.com"
        val headers = mapOf("Content-Type" to listOf("application/json"))

        mockkStatic(HttpClient::class) {
            every { HttpClient.post(url, headers = headers, body = json) } returns response

            val teamsNotifier = TeamsNotifier()
            teamsNotifier.send(acMessage, url)

            verify(exactly = 1) { HttpClient.post(url, headers = headers, body = json) }
        }
    }

    @Test
    fun `it should throw MessageFailedToSendException on status code != 202`() {
        val response: HttpResponse<String> = mockResponse(403)
        val (acMessage, json) = buildMessageBodyFor("Hello World!")
        val url = "https://1234jkl.webhook.office.com"
        val headers = mapOf("Content-Type" to listOf("application/json"))

        mockkStatic(HttpClient::class) {
            every {
                HttpClient.post(
                    url,
                    headers = headers,
                    body = json
                )
            } returns response

            val teamsNotifier = TeamsNotifier()
            assertThrows<MessageFailedToSendException> { teamsNotifier.send(acMessage, url) }

            verify(exactly = 1) { HttpClient.post(url, headers = headers, body = json) }
        }
    }

    @Test
    fun `it should throw exceptions on failure to send`() {
        val (acMessage, json) = buildMessageBodyFor("Hello World!")
        val url = "https://1234jkl.webhook.office.com"
        val headers = mapOf("Content-Type" to listOf("application/json"))

        mockkStatic(HttpClient::class) {
            every {
                HttpClient.post(
                    url,
                    headers = headers,
                    body = json
                )
            } throws IOException("err")

            val teamsNotifier = TeamsNotifier()
            assertThrows<MessageFailedToSendException> { teamsNotifier.send(acMessage, url) }

            verify(exactly = 1) { HttpClient.post(url, headers = headers, body = json) }
        }
    }

    private fun mockResponse(status: Int = 202, body: String? = "OK"): HttpResponse<String> {
        val response: HttpResponse<String> = mockk()
        every { response.statusCode() } returns status
        every { response.body() } returns body
        return response
    }

    private fun buildMessageBodyFor(message: String): Pair<AlerticornMessage, String> {
        val acMessage = AlerticornMessage(
            title = message,
        )
        return Pair(acMessage, acMessage.toTeamsWebHookMessage().toJson())
    }
}