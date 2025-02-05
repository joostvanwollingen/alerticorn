package nl.vanwollingen.alerticorn.slack

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SlackWebHookMessageTest {

    @Test
    fun `it should convert message to JSON body`() {
        val message = SlackWebHookMessage(
            text = "Hello World!",
            iconEmoji = ":smile:",
            username = "TestUser",
            channelOverride = "#general"
        )
        val expectedJson =
            """{"text": "Hello World!", "icon_emoji": ":smile:", "username": "TestUser", "channel": "#general"}"""
        assertEquals(expectedJson, message.toJson())
    }

    @Test
    fun `it should convert message with attachments to JSON body`() {
        val attachment = SlackWebHookMessage.Attachment(
            fallback = "Fallback text",
            pretext = "Pretext",
            color = "good",
            fields = listOf(
                SlackWebHookMessage.Attachment.Field(title = "Title1", value = "Value1", short = true),
                SlackWebHookMessage.Attachment.Field(title = "Title2", value = "Value2", short = false)
            )
        )
        val message = SlackWebHookMessage(
            text = "Hello World!",
            attachments = listOf(attachment)
        )
        val expectedJson =
            """{"text": "Hello World!", "icon_emoji": ":unicorn_face:", "username": "Alerticorn", "attachments": [{"fallback": "Fallback text", "pretext": "Pretext", "color": "good", "fields": [{"title": "Title1", "value": "Value1", "short": true }, {"title": "Title2", "value": "Value2", "short": false }]}]}"""
        assertEquals(expectedJson, message.toJson())
    }
}