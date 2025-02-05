package nl.vanwollingen.alerticorn.discord

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DiscordWebhookMessageTest {

    @Test
    fun `it should convert message to JSON body`() {
        val message = DiscordWebhookMessage(
            content = "Hello World!",
            username = "TestUser",
            avatarUrl = "https://example.com/avatar.png"
        )
        val expectedJson = """{"content": "Hello World!", "username": "TestUser", "avatar_url": "https://example.com/avatar.png"}"""
        assertEquals(expectedJson, message.toJson().lines().joinToString("") { it.trimIndent() })
    }

    @Test
    fun `it should convert message with embeds to JSON body`() {
        val embed = DiscordWebhookMessage.Embed(
            title = "Embed Title",
            description = "Embed Description",
            url = "https://example.com",
            fields = listOf(
                DiscordWebhookMessage.Embed.Field(name = "Field1", value = "Value1", inline = true),
                DiscordWebhookMessage.Embed.Field(name = "Field2", value = "Value2", inline = false)
            )
        )
        val message = DiscordWebhookMessage(
            content = "Hello World!",
            embeds = listOf(embed)
        )
        val expectedJson = """{"content": "Hello World!", "avatar_url": "https://cdn.discordapp.com/avatars/1333554134481371176/1f5c89c8a182902e05fa4c20c7a59271.webp", "embeds": [{"title": "Embed Title", "description": "Embed Description", "url": "https://example.com", "type": "rich", "fields": [{"name": "Field1", "value": "Value1", "inline": true},{"name": "Field2", "value": "Value2", "inline": false}]}]}"""
        assertEquals(expectedJson, message.toJson())
    }
}
