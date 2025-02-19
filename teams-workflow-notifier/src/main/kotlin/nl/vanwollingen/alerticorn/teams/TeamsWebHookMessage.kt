package nl.vanwollingen.alerticorn.teams

import nl.vanwollingen.alerticorn.api.AlerticornMessage

/**
 * Represents a message format for sending alerts to Teams via Incoming Webhook With Workflows.
 *
 * The [TeamsWebHookMessage] class is used to structure the message payload for Teams Webhooks. It contains an array of
 * attachments, in the form of ([adaptive cards](https://learn.microsoft.com/en-us/power-automate/create-adaptive-cards)).
 *
 * @property attachments the list of adaptive cards to include in the message
 */
data class TeamsWebHookMessage(
    val attachments: List<CardElement>
) {
    /**
     * Converts the [TeamsWebHookMessage] to a JSON string format.
     *
     * This method constructs the JSON payload that can be sent to the Teams Webhook API.
     *
     * @return a JSON string representing the Teams webhook message
     */
    fun toJson(): String = """
        {
            "type": "message",
            "attachments": [
                {
                "contentType": "application/vnd.microsoft.card.adaptive",
                "content": {
                    "type": "AdaptiveCard",
                    "body": [${attachments.joinToString(",") { it.toJson() }}]
                }
                }
            ]
        }
    """.lines().joinToString("") { it.trimIndent() }

    sealed class CardElement {
        abstract fun toJson(): String
    }

    data class RichTextBlock(
        val inlines: List<TextRun>
    ) : CardElement() {
        override fun toJson(): String = """
        {
            "type": "RichTextBlock",
            "inlines": [${inlines.joinToString(",") { it.toJson() }}]
        }
    """.lines().joinToString("") { it.trimIndent() }
    }

    data class TextRun(val textBlock: TextBlock) : CardElement() {
        override fun toJson(): String = """
        {
            "type": "TextRun",
            "text": "${textBlock.text}"
            ${textBlock.size?.let { ""","size": "$it"""" } ?: ""}
            ${textBlock.weight?.let { ""","weight": "$it"""" } ?: ""}
            ${textBlock.color?.let { ""","color": "$it"""" } ?: ""}
            ${if (textBlock.wrap) ""","wrap": true""" else ""}
        }
    """.lines().joinToString("") { it.trimIndent() }
    }

    data class TextBlock @JvmOverloads constructor(
        val text: String,
        val size: String? = null,
        val weight: String? = null,
        val color: String? = null,
        val wrap: Boolean = false
    ) : CardElement() {
        override fun toJson(): String = """
        {
            "type": "TextBlock",
            "text": "$text"
            ${size?.let { ""","size": "$it"""" } ?: ""}
            ${weight?.let { ""","weight": "$it"""" } ?: ""}
            ${color?.let { ""","color": "$it"""" } ?: ""}
            ${if (wrap) ""","wrap": true""" else ""}
        }
    """.lines().joinToString("") { it.trimIndent() }
    }

    data class FactSet(
        val facts: List<Fact>
    ) : CardElement() {
        override fun toJson(): String = """
        {
            "type": "FactSet",
            "facts": [${facts.joinToString(",") { it.toJson() }}]
        }
    """.lines().joinToString("") { it.trimIndent() }
    }

    data class Fact(
        val title: String, val value: String
    ) {
        fun toJson(): String = """
        {
            "title": "$title",
            "value": "$value"
        }
    """.lines().joinToString("") { it.trimIndent() }
    }
}

/**
 * Converts an [AlerticornMessage] to a [TeamsWebHookMessage].
 *
 * This extension function creates a [TeamsWebHookMessage] based on the information available
 * in an [AlerticornMessage], including title, body, links, details, and throwable data.
 *
 * The generated [TeamsWebHookMessage] includes the alert message text, formatted attachments for links,
 * details, and any exception data if available.
 *
 * @return A [TeamsWebHookMessage] that represents the alert message, ready to be sent to a Teams Webhook
 */
fun AlerticornMessage.toTeamsWebHookMessage(): TeamsWebHookMessage {
    val titleBlock = TeamsWebHookMessage.RichTextBlock(
        listOf(
            TeamsWebHookMessage.TextRun(
                TeamsWebHookMessage.TextBlock(
                    title,
                    size = "medium",
                    weight = "bolder"
                )
            )
        )
    )

    val bodyBlock = body?.let {
        TeamsWebHookMessage.RichTextBlock(
            listOf(
                TeamsWebHookMessage.TextRun(
                    TeamsWebHookMessage.TextBlock(
                        text = it,
                        size = "medium",
                    )
                )
            )
        )
    }

    val detailsBlock = details?.let { item ->
        TeamsWebHookMessage.FactSet(
            item.map { TeamsWebHookMessage.Fact(it.key, it.value) }
        )
    }

    val linkBlock = links?.let {
        TeamsWebHookMessage.FactSet(
            it.map { TeamsWebHookMessage.Fact("[\uD83D\uDD17](${it.value})", "[${it.key}](${it.value})") }
        )
    }

    val throwableBlock = throwable?.let {
        TeamsWebHookMessage.FactSet(
            listOf(
                TeamsWebHookMessage.Fact("Exception", it.message ?: ""),
                TeamsWebHookMessage.Fact("Cause", it.cause.toString()),
            )
        )
    }

    return TeamsWebHookMessage(
        attachments = listOfNotNull(titleBlock, bodyBlock, throwableBlock, detailsBlock, linkBlock)
    )
}
