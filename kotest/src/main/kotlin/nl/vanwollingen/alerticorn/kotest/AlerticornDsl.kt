package nl.vanwollingen.alerticorn.kotest

import io.kotest.core.extensions.TestCaseExtension
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.core.spec.style.scopes.FunSpecRootScope
import io.kotest.core.test.TestScope
import nl.vanwollingen.alerticorn.api.Event

/**
 * Creates an [AlerticornTestCaseExtension] for use with Kotest's `.config(extensions = ...)`.
 *
 * This builder function works with **all** Kotest spec styles:
 *
 * ```kotlin
 * test("my test").config(
 *     extensions = listOf(alerticorn(title = "Test failed", events = listOf(Event.FAIL)))
 * ) {
 *     // test body
 * }
 * ```
 *
 * @param title The title of the notification message.
 * @param body The optional body content.
 * @param details Additional details as key-value pairs.
 * @param links Related links as key-value pairs.
 * @param events The events that trigger notifications. Empty list means all events.
 * @param platform The target platform (e.g., "slack", "discord").
 * @param channel The target channel name.
 * @param template The template key for `MessageProvider`-based messages.
 * @return A [TestCaseExtension] configured with the specified notification settings.
 */
fun alerticorn(
    title: String,
    body: String = "",
    details: Map<String, String> = emptyMap(),
    links: Map<String, String> = emptyMap(),
    events: List<Event> = emptyList(),
    platform: String? = null,
    channel: String? = null,
    template: String? = null,
): TestCaseExtension = AlerticornTestCaseExtension(
    title = title,
    body = body,
    details = details,
    links = links,
    events = events,
    platform = platform,
    channel = channel,
    template = template,
)

/**
 * Registers a test with Alerticorn notification support in a [FunSpecRootScope] (top-level).
 *
 * ```kotlin
 * class MySpec : FunSpec({
 *     alerticornTest("critical test", title = "Failed!", events = listOf(Event.FAIL)) {
 *         // test body
 *     }
 * })
 * ```
 *
 * @param name The test name.
 * @param title The notification message title.
 * @param body The optional notification body.
 * @param details Additional details as key-value pairs.
 * @param links Related links as key-value pairs.
 * @param events The events that trigger notifications. Empty list means all events.
 * @param platform The target platform.
 * @param channel The target channel name.
 * @param template The template key for `MessageProvider`-based messages.
 * @param test The test body.
 */
fun FunSpecRootScope.alerticornTest(
    name: String,
    title: String,
    body: String = "",
    details: Map<String, String> = emptyMap(),
    links: Map<String, String> = emptyMap(),
    events: List<Event> = emptyList(),
    platform: String? = null,
    channel: String? = null,
    template: String? = null,
    test: suspend TestScope.() -> Unit,
) {
    test(name).config(
        extensions = listOf(
            AlerticornTestCaseExtension(
                title = title,
                body = body,
                details = details,
                links = links,
                events = events,
                platform = platform,
                channel = channel,
                template = template,
            )
        ),
        test = test,
    )
}

/**
 * Registers a test with Alerticorn notification support inside a [FunSpecContainerScope] (nested context).
 *
 * @see [FunSpecRootScope.alerticornTest] for parameter descriptions.
 */
suspend fun FunSpecContainerScope.alerticornTest(
    name: String,
    title: String,
    body: String = "",
    details: Map<String, String> = emptyMap(),
    links: Map<String, String> = emptyMap(),
    events: List<Event> = emptyList(),
    platform: String? = null,
    channel: String? = null,
    template: String? = null,
    test: suspend TestScope.() -> Unit,
) {
    test(name).config(
        extensions = listOf(
            AlerticornTestCaseExtension(
                title = title,
                body = body,
                details = details,
                links = links,
                events = events,
                platform = platform,
                channel = channel,
                template = template,
            )
        ),
        test = test,
    )
}
