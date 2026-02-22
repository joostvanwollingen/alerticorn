package nl.vanwollingen.alerticorn.kotest

import io.kotest.core.extensions.TestCaseExtension
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import nl.vanwollingen.alerticorn.api.*

/**
 * A per-test Kotest extension carrying its own message configuration.
 *
 * Use this when you need per-test notification settings that override any spec-level
 * [AlerticornExtension] configuration. Register via `.config(extensions = ...)`:
 *
 * ```kotlin
 * test("critical test").config(
 *     extensions = listOf(AlerticornTestCaseExtension(
 *         title = "Critical test failed!",
 *         events = listOf(Event.FAIL),
 *         platform = "slack",
 *         channel = "critical-alerts"
 *     ))
 * ) {
 *     // test body
 * }
 * ```
 *
 * Or use the [alerticorn] DSL builder for a more concise syntax.
 *
 * @param title The title of the notification message.
 * @param body The optional body content of the message.
 * @param details Additional details as key-value pairs.
 * @param links Related links as key-value pairs.
 * @param events The events that trigger notifications. Empty list means all events.
 * @param platform The target platform (e.g., "slack", "discord").
 * @param channel The target channel name.
 * @param template The template key for [MessageProvider]-based messages.
 */
class AlerticornTestCaseExtension(
    private val title: String,
    private val body: String = "",
    private val details: Map<String, String> = emptyMap(),
    private val links: Map<String, String> = emptyMap(),
    private val events: List<Event> = emptyList(),
    private val platform: String? = null,
    private val channel: String? = null,
    private val template: String? = null,
) : TestCaseExtension {

    override suspend fun intercept(testCase: TestCase, execute: suspend (TestCase) -> TestResult): TestResult {
        val result = execute(testCase)
        val resultEvents = AlerticornExtension.mapResultToEvents(result)

        for (event in resultEvents) {
            if (shouldNotify(event)) {
                buildMessage(result.errorOrNull)?.let { message ->
                    notify(message)
                }
                break // Send at most one notification per test outcome
            }
        }

        return result
    }

    private fun shouldNotify(event: Event): Boolean {
        if (events.isEmpty()) return true
        return events.contains(event) || events.contains(Event.ANY)
    }

    private fun buildMessage(throwable: Throwable?): AlerticornMessage? {
        if (title.isBlank()) return null

        if (template != null) {
            return MessageProvider.run(template, title, throwable)
        }

        return AlerticornMessage(title, body, details, links, throwable)
    }

    private fun notify(message: AlerticornMessage) {
        try {
            NotificationManager.notify(
                platform = platform, message = message, destination = channel
            )
        } catch (e: MessageFailedToSendException) {
            println(e.stackTraceToString())
        }
    }
}
