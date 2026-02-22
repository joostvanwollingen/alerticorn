package nl.vanwollingen.alerticorn.slack

import nl.vanwollingen.alerticorn.api.AlerticornMessage

/**
 * Represents a message format for sending alerts to Slack via webhook.
 *
 * The [SlackWebHookMessage] class is used to structure the message payload for Slack Webhooks. It allows
 * customization of the message text, username, emoji icon, channel, and attachments. The `attachments` section
 * allows the inclusion of rich content for better message organization.
 *
 * @property text the main message text
 * @property iconEmoji the emoji used for the message icon (default is ":unicorn_face:")
 * @property username the username that will appear as the message sender (default is "Alerticorn")
 * @property channelOverride the Slack channel to override the default channel (optional)
 * @property attachments the list of attachments to include in the message
 */
data class SlackWebHookMessage @JvmOverloads constructor(
    val text: String? = null,
    val iconEmoji: String? = ":unicorn_face:",
    val username: String? = "Alerticorn",
    val channelOverride: String? = null,
    val attachments: List<Attachment>? = null
) {
    /**
     * Converts the [SlackWebHookMessage] to a JSON string format.
     *
     * This method constructs the JSON payload that can be sent to the Slack Webhook API.
     * It includes the message text, icon emoji, username, optional channel override, and attachments.
     *
     * @return a JSON string representing the Slack webhook message
     */
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

    /**
     * Represents an attachment in the Slack message, which can contain rich content such as fields and pretext.
     *
     * @property fallback a plain text summary of the attachment (optional)
     * @property pretext text that appears above the attachment (optional)
     * @property color the color of the attachment (default is "info")
     * @property fields a list of fields to display within the attachment
     */
    data class Attachment @JvmOverloads constructor(
        val fallback: String? = null,
        val pretext: String? = null,
        val color: String? = "info",
        val fields: List<Field>? = null
    ) {
        /**
         * Converts the [Attachment] to a JSON string format.
         *
         * This method converts the attachment to a valid JSON format to be included in the Slack message.
         *
         * @return a JSON string representing the attachment
         */
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

        /**
         * Represents a field within a Slack attachment.
         *
         * @property title the field title
         * @property value the field value
         * @property short whether the field is short (fits side by side with other fields)
         */
        data class Field @JvmOverloads constructor(val title: String, val value: String, val short: Boolean = false)
    }
}

/**
 * Converts an [AlerticornMessage] to a [SlackWebHookMessage].
 *
 * This extension function creates a [SlackWebHookMessage] based on the information available
 * in an [AlerticornMessage], including title, body, links, details, and throwable data.
 *
 * The generated [SlackWebHookMessage] includes the alert message text, formatted attachments for links,
 * details, and any exception data if available.
 *
 * @return A [SlackWebHookMessage] that represents the alert message, ready to be sent to a Slack Webhook
 */
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
                    throwable?.message ?: "",
                    throwable?.cause?.toString() ?: ""
                )
            )
        )
    }


    return SlackWebHookMessage(
        text = getIfNotEmpty(title) + "\n" + getIfNotEmpty(body),
        attachments = listOfNotNull(throwable, details, links),
    )
}