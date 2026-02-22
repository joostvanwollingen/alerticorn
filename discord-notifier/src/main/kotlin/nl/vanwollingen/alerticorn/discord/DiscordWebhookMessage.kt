package nl.vanwollingen.alerticorn.discord

import nl.vanwollingen.alerticorn.api.AlerticornMessage

/**
 * Represents the message format for sending messages to Discord webhooks.
 *
 * This class models the structure of a Discord webhook message, which can contain a content string,
 * a username, an avatar URL, and a list of embedded objects. The message is converted to a JSON
 * string via the [toJson] method, which is then sent to Discord via the webhook.
 *
 * @property content The content of the message (optional).
 * @property username The username for the message sender (optional).
 * @property avatarUrl The URL of the avatar for the message sender (defaults to a Discord avatar).
 * @property embeds A list of embeds that can be included in the message (optional).
 */
data class DiscordWebhookMessage @JvmOverloads constructor(
    val content: String? = null,
    val username: String? = null,
    val avatarUrl: String? = "https://cdn.discordapp.com/avatars/1333554134481371176/1f5c89c8a182902e05fa4c20c7a59271.webp",
    val embeds: List<Embed>? = null
) {

    /**
     * Converts the [DiscordWebhookMessage] to a JSON string representation.
     *
     * This method serializes the message object into the JSON format required by Discord webhooks,
     * including the content, username, avatar URL, and embeds, if provided.
     *
     * @return A JSON string representing the webhook message.
     */
    fun toJson(): String {
        val sb = StringBuilder()
        sb.append("{")
        content?.let { sb.append(""""content": "$content", """) }
        username?.let { sb.append(""""username": "$username", """) }
        avatarUrl?.let { sb.append(""""avatar_url": "$avatarUrl", """) }
        embeds?.let {
            sb.append(""""embeds": [""")
            sb.append(it.joinToString(",") { embed ->
                embed.toJson()
            })
            sb.append("]")
        }
        if (sb.endsWith(", ")) sb.setLength(sb.length - 2)
        sb.append("}")
        return sb.toString()
    }

    /**
     * Represents an embed within a Discord webhook message.
     *
     * An embed can contain a title, description, URL, type, and fields, which are additional
     * pieces of information formatted as key-value pairs.
     *
     * @property title The title of the embed (optional).
     * @property description The description of the embed (optional).
     * @property url The URL associated with the embed (optional).
     * @property type The type of embed, which is always "rich" (default).
     * @property fields A list of fields to include in the embed (optional).
     */
    data class Embed @JvmOverloads constructor(
        val title: String? = null,
        val description: String? = null,
        val url: String? = null,
        val type: String = "rich",
        val fields: List<Field>? = null
    ) {

        /**
         * Converts the [Embed] to a JSON string representation.
         *
         * This method serializes the embed object into the JSON format required by Discord,
         * including the title, description, URL, type, and fields, if provided.
         *
         * @return A JSON string representing the embed.
         */
        fun toJson(): String {
            val sb = StringBuilder()
            sb.append("{")
            title?.let { sb.append(""""title": "$title", """) }
            description?.let { sb.append(""""description": "$description", """) }
            url?.let { sb.append(""""url": "$url", """) }
            sb.append(""""type": "$type", """)
            fields?.let {
                sb.append(""""fields": [""")
                sb.append(
                    fields.joinToString(",") { field ->
                        """{"name": "${field.name}", "value": "${field.value}", "inline": ${field.inline}}"""
                    }
                )
                sb.append("], ")
            }
            if (sb.endsWith(", ")) sb.setLength(sb.length - 2)
            sb.append("}")
            return sb.toString()
        }

        /**
         * Represents a field in a Discord embed.
         *
         * A field consists of a name, value, and an optional flag indicating whether it should be
         * displayed inline with other fields.
         *
         * @property name The name of the field.
         * @property value The value of the field.
         * @property inline Whether the field should be displayed inline (default is false).
         */
        data class Field @JvmOverloads constructor(val name: String, val value: String, val inline: Boolean = false)
    }
}

/**
 * Converts an [AlerticornMessage] to a [DiscordWebhookMessage].
 *
 * This extension function creates a [DiscordWebhookMessage] based on the information available
 * in an [AlerticornMessage], including title, body, links, details, and throwable data.
 *
 * @return A [DiscordWebhookMessage] that represents the alert message.
 */
fun AlerticornMessage.toDiscordWebhookMessage(): DiscordWebhookMessage {
    fun getIfNotEmpty(string: String?) = if (string?.isNotEmpty() == true) string else ""

    val links: DiscordWebhookMessage.Embed? = if (links?.isNotEmpty() == true) {
        DiscordWebhookMessage.Embed(
            title = "Links", fields = links?.map { DiscordWebhookMessage.Embed.Field("", "[${it.key}](${it.value})") })
    } else null

    val details: DiscordWebhookMessage.Embed? = if (details?.isNotEmpty() == true) {
        DiscordWebhookMessage.Embed(
            title = "Details", fields = details?.map { DiscordWebhookMessage.Embed.Field(it.key, it.value) })
    } else null

    val throwable: DiscordWebhookMessage.Embed? = throwable?.let {
        DiscordWebhookMessage.Embed(
            title = "Exception", fields = listOf(
                DiscordWebhookMessage.Embed.Field(
                    "Message", throwable?.message ?: ""
                ), DiscordWebhookMessage.Embed.Field(
                    "Cause", throwable?.cause?.toString() ?: ""
                )
            )
        )
    }

    return DiscordWebhookMessage(
        content = getIfNotEmpty(title) + if (body?.isNotEmpty() == true) {
            "\\n" + getIfNotEmpty(body)
        } else "",
        embeds = listOfNotNull(throwable, details, links),
    )
}