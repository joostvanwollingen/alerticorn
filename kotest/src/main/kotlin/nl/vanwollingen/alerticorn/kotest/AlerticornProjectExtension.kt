package nl.vanwollingen.alerticorn.kotest

import io.kotest.core.listeners.AfterProjectListener
import io.kotest.core.listeners.AfterTestListener
import io.kotest.core.listeners.BeforeProjectListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import nl.vanwollingen.alerticorn.api.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Kotest project-level extension for sending notifications at the start and end of a test run.
 *
 * Provides `Event.SUITE_START` and `Event.SUITE_COMPLETE` events. Since Kotest's `afterProject()`
 * does not receive a results summary, this extension also tracks test results internally via
 * [AfterTestListener] and includes the counts in the `SUITE_COMPLETE` notification.
 *
 * Register via `ProjectConfig`:
 * ```kotlin
 * class ProjectConfig : AbstractProjectConfig() {
 *     override fun extensions() = listOf(
 *         AlerticornProjectExtension(
 *             title = "Test suite finished",
 *             platform = "slack",
 *             channel = "ci-results",
 *             events = listOf(Event.SUITE_COMPLETE)
 *         )
 *     )
 * }
 * ```
 *
 * @param title The title of the notification message.
 * @param body The optional body content of the message.
 * @param events The events that trigger notifications. Defaults to `SUITE_COMPLETE`.
 * @param platform The target platform (e.g., "slack", "discord").
 * @param channel The target channel name.
 */
class AlerticornProjectExtension(
    private val title: String,
    private val body: String = "",
    private val events: List<Event> = listOf(Event.SUITE_COMPLETE),
    private val platform: String? = null,
    private val channel: String? = null,
) : BeforeProjectListener, AfterProjectListener, AfterTestListener {

    private val passed = AtomicInteger(0)
    private val failed = AtomicInteger(0)
    private val errors = AtomicInteger(0)
    private val ignored = AtomicInteger(0)

    override suspend fun afterTest(testCase: TestCase, result: TestResult) {
        afterAny(testCase, result)
    }

    override suspend fun afterAny(testCase: TestCase, result: TestResult) {
        when (result) {
            is TestResult.Success -> passed.incrementAndGet()
            is TestResult.Failure -> failed.incrementAndGet()
            is TestResult.Error -> errors.incrementAndGet()
            is TestResult.Ignored -> ignored.incrementAndGet()
        }
    }

    override suspend fun beforeProject() {
        if (events.contains(Event.SUITE_START) || events.contains(Event.ANY)) {
            val message = AlerticornMessage(title = title, body = body)
            notify(message)
        }
    }

    override suspend fun afterProject() {
        if (events.contains(Event.SUITE_COMPLETE) || events.contains(Event.ANY)) {
            val total = passed.get() + failed.get() + errors.get() + ignored.get()
            val details = mutableMapOf(
                "total" to total.toString(),
                "passed" to passed.get().toString(),
                "failed" to failed.get().toString(),
                "errors" to errors.get().toString(),
                "ignored" to ignored.get().toString(),
            )

            val message = AlerticornMessage(title = title, body = body, details = details)
            notify(message)
        }
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
