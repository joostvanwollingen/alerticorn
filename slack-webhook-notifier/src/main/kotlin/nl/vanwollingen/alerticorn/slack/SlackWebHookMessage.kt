package nl.vanwollingen.alerticorn.slack

import nl.vanwollingen.alerticorn.api.AlerticornMessage

data class SlackWebHookMessage(
    val text: String? = null,
    val iconEmoji: String? = ":unicorn_face:",
    val username: String? = "Alerticorn",
    val channelOverride: String? = null,
    val attachments: List<Attachment>? = null
) {
    fun toJson(): String {
        val sb = StringBuilder()
        sb.append("{")
        text?.let { sb.append(""""text": "$text", """) }
        iconEmoji?.let { sb.append(""""icon_emoji": "$iconEmoji", """) }
        username?.let { sb.append(""""username": "$username", """) }
        channelOverride?.let { sb.append(""""channel": "$channelOverride", """) }
        attachments?.let {
            sb.append(""""attachments": [""")
            sb.append(
                it.joinToString(", ") { attachment -> attachment.toJson() }
            )
            sb.append("], ")
        }
        if (sb.endsWith(", ")) sb.setLength(sb.length - 2)
        sb.append("}")
        return sb.toString()
    }

    data class Attachment(
        val fallback: String? = null,
        val pretext: String? = null,
        val color: String? = "info",
        val fields: List<Field>? = null
    ) {
        fun toJson(): String {
            val sb = StringBuilder()
            sb.append("{")
            fallback?.let { sb.append(""""fallback": "$fallback", """) }
            pretext?.let { sb.append(""""pretext": "$pretext", """) }
            color?.let { sb.append(""""color": "$color", """) }
            fields?.let {
                sb.append(""""fields": [""")
                sb.append(
                    it.joinToString(", ") {
                        """{"title": "${it.title}", "value": "${it.value}", "short": ${it.short} }"""
                    }
                )
                sb.append("], ")
            }
            if (sb.endsWith(", ")) sb.setLength(sb.length - 2)
            sb.append("}")
            return sb.toString()
        }

        data class Field(val title: String, val value: String, val short: Boolean = false)
    }
}

fun AlerticornMessage.toSlackWebHookMessage(): SlackWebHookMessage {
    fun getIfNotEmpty(string: String?) = if (string?.isNotEmpty() == true) "*$string*" else ""

    //TODO nullability on message via annotation
    val links: SlackWebHookMessage.Attachment? = if (links?.isNotEmpty() == true) {
        SlackWebHookMessage.Attachment(
            pretext = "*Links*",
            fields = links?.map { SlackWebHookMessage.Attachment.Field("", "<${it.value}|${it.key}>") }
        )
    } else null

    val details: SlackWebHookMessage.Attachment? = if (details?.isNotEmpty() == true) {
        SlackWebHookMessage.Attachment(
            pretext = "*Details*",
            fields = details?.map { SlackWebHookMessage.Attachment.Field(it.key, it.value) }
        )
    } else null

    val throwable: SlackWebHookMessage.Attachment? = throwable?.let {
        SlackWebHookMessage.Attachment(
            pretext = "*Exception*",
            color = "danger",
            fields = listOf(
                SlackWebHookMessage.Attachment.Field(
                    throwable?.message.toString(),
                    throwable?.cause.toString()
                )
            )
        )
    }


    return SlackWebHookMessage(
        text = getIfNotEmpty(title) + "\n" + getIfNotEmpty(body),
        attachments = listOfNotNull(throwable, details, links),
    )
}