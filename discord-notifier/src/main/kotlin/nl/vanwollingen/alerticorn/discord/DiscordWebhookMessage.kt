package nl.vanwollingen.alerticorn.discord

import nl.vanwollingen.alerticorn.api.AlerticornMessage

data class DiscordWebhookMessage @JvmOverloads constructor(
    val content: String? = null,
    val username: String? = null,
    val avatarUrl: String? = "https://cdn.discordapp.com/avatars/1333554134481371176/1f5c89c8a182902e05fa4c20c7a59271.webp",
    val embeds: List<Embed>? = null
) {
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

    data class Embed @JvmOverloads constructor(
        val title: String? = null,
        val description: String? = null,
        val url: String? = null,
        val type: String = "rich",
        val fields: List<Field>? = null
    ) {
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

        data class Field @JvmOverloads constructor(val name: String, val value: String, val inline: Boolean = false)
    }
}

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
                    "Message", throwable?.message.toString()
                ), DiscordWebhookMessage.Embed.Field(
                    "Cause", throwable?.cause.toString()
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
