package nl.vanwollingen.alerticorn.discord

import io.mockk.*
import nl.vanwollingen.alerticorn.api.AlerticornMessage
import nl.vanwollingen.alerticorn.api.MessageFailedToSendException
import nl.vanwollingen.alerticorn.http.HttpClient
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.net.http.HttpHeaders
import java.net.http.HttpResponse
import kotlin.test.assertEquals

class DiscordNotifierTest {

    private val headers = mapOf("Content-Type" to listOf("application/json"))

    @Test
    fun `it should notify`() {
        val response: HttpResponse<String> = mockResponse()

        val (acMessage, json) = buildMessageBodyFor("Hello World!")
        val uri = "https://discord.com/webhook"

        mockkStatic(HttpClient::class) {
            every { HttpClient.post(uri = uri, headers = headers, body = json) } returns response

            val discordNotifier = DiscordNotifier()
            discordNotifier.send(acMessage, uri)

            verify(exactly = 1) { HttpClient.post(uri = uri, headers = headers, body = json) }
        }
    }

    @Test
    fun `it should throw MessageFailedToSendException if response code != 204`() {
        val response: HttpResponse<String> = mockResponse(
            status = 400,
            body = "Caller did something wrong"
        )

        val (acMessage, json) = buildMessageBodyFor("Hello World!")
        val uri = "https://discord.com/webhook"

        mockkStatic(HttpClient::class) {
            every { HttpClient.post(uri = uri, headers = headers, body = json) } returns response

            val discordNotifier = DiscordNotifier()
            val exception = assertThrows<MessageFailedToSendException> { discordNotifier.send(acMessage, uri) }

            verify(exactly = 1) { HttpClient.post(uri = uri, headers = headers, body = json) }

            assertEquals(
                "Failed to send message to Discord: HTTP 400 {Content-Type=[application/json]} Caller did something wrong",
                exception.message
            )
        }
    }

    @Test
    fun `it should throw exceptions on failure to send`() {
        val (acMessage, json) = buildMessageBodyFor("Hello World!")
        val uri = "https://discord.com/webhook"

        mockkStatic(HttpClient::class) {
            every {
                HttpClient.post(
                    uri = uri, headers = headers, body = json
                )
            } throws IOException("err")

            val discordNotifier = DiscordNotifier()
            assertThrows<MessageFailedToSendException> { discordNotifier.send(acMessage, uri) }

            verify(exactly = 1) {
                HttpClient.post(
                    uri = uri, headers = headers, body = json
                )
            }
        }
    }

    private fun mockResponse(
        status: Int = 204, body: String? = null, headers: HttpHeaders = HttpHeaders.of(
            mapOf("Content-Type" to listOf("application/json")), { _, _ -> true })
    ): HttpResponse<String> {
        val response: HttpResponse<String> = mockk()
        every { response.statusCode() } returns status
        every { response.body() } returns body
        every { response.headers() } returns headers
        return response
    }

    private fun buildMessageBodyFor(message: String): Pair<AlerticornMessage, String> {
        val acMessage = AlerticornMessage(
            title = message,
        )
        return Pair(acMessage, acMessage.toDiscordWebhookMessage().toJson())
    }
}
