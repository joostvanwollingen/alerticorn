package nl.vanwollingen.alerticorn.kotest

import io.kotest.core.listeners.AfterTestListener
import io.kotest.core.extensions.TestCaseExtension
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import nl.vanwollingen.alerticorn.api.*
import java.lang.reflect.AnnotatedElement

/**
 * Kotest extension for integrating Alerticorn notifications with test execution events.
 *
 * This extension reads `@Message` annotations from the **Spec class** and sends notifications
 * based on test outcomes. It can be registered per-spec via `extensions(AlerticornExtension())`
 * in the spec init block, or globally via `ProjectConfig`.
 *
 * For `AnnotationSpec`, method-level annotations are also checked and take precedence over
 * class-level annotations, matching the JUnit/TestNG behavior.
 *
 * Event mapping:
 * - `TestResult.Success` -> `Event.PASS`
 * - `TestResult.Failure` (assertion failure) -> `Event.FAIL`
 * - `TestResult.Error` (unexpected exception) -> `Event.FAIL` + `Event.EXCEPTION`
 * - `TestResult.Ignored` -> `Event.SKIP` / `Event.DISABLED`
 *
 * @see Message
 */
class AlerticornExtension : AfterTestListener, TestCaseExtension {

    override suspend fun afterTest(testCase: TestCase, result: TestResult) {
        afterAny(testCase, result)
    }

    override suspend fun afterAny(testCase: TestCase, result: TestResult) {
        val specClass = testCase.spec::class.java
        val method = resolveMethod(testCase, specClass)
        val details = getAnnotationDetails(method, specClass)

        val events = mapResultToEvents(result)
        val throwable = result.errorOrNull

        for (event in events) {
            if (shouldNotify(details.events, event)) {
                getAlerticornMessage(details.message, details.template, throwable)?.let {
                    notify(details.platform, it, details.channel)
                }
                break // Send at most one notification per test outcome
            }
        }
    }

    override suspend fun intercept(testCase: TestCase, execute: suspend (TestCase) -> TestResult): TestResult {
        val specClass = testCase.spec::class.java
        val method = resolveMethod(testCase, specClass)
        val details = getAnnotationDetails(method, specClass)

        return try {
            execute(testCase)
        } catch (t: Throwable) {
            if (shouldNotify(details.events, Event.EXCEPTION)) {
                getAlerticornMessage(details.message, details.template, t)?.let {
                    notify(details.platform, it, details.channel)
                }
            }
            throw t
        }
    }

    private fun resolveMethod(testCase: TestCase, specClass: Class<*>): java.lang.reflect.Method? {
        val testName = testCase.name.testName
        return try {
            specClass.getMethod(testName)
        } catch (_: NoSuchMethodException) {
            try {
                specClass.getDeclaredMethod(testName)
            } catch (_: NoSuchMethodException) {
                null
            }
        }
    }

    internal fun getAnnotationDetails(
        primary: AnnotatedElement?, secondary: AnnotatedElement?
    ): AnnotationDetails {
        val message = getAnnotation<Message>(primary, secondary)
        val events = getAnnotation<Message.Events>(primary, secondary)
        val channel = getAnnotation<Message.Channel>(primary, secondary)
        val template = getAnnotation<Message.Template>(primary, secondary)
        val platform = getAnnotation<Message.Platform>(primary, secondary)

        return AnnotationDetails(message, events, channel, template, platform)
    }

    internal data class AnnotationDetails(
        val message: Message? = null,
        val events: Message.Events? = null,
        val channel: Message.Channel? = null,
        val template: Message.Template? = null,
        val platform: Message.Platform? = null,
    )

    private inline fun <reified T : Annotation> getAnnotation(
        primary: AnnotatedElement?, secondary: AnnotatedElement?
    ): T? {
        return primary?.getAnnotation(T::class.java)
            ?: secondary?.getAnnotation(T::class.java)
    }

    internal fun shouldNotify(eventsAnnotation: Message.Events?, event: Event): Boolean {
        if (eventsAnnotation == null) return true
        return eventsAnnotation.value.contains(event) || eventsAnnotation.value.contains(Event.ANY)
    }

    internal fun getAlerticornMessage(
        messageAnnotation: Message?, templateAnnotation: Message.Template?, throwable: Throwable?
    ): AlerticornMessage? {
        val title = messageAnnotation?.title ?: return null

        if (templateAnnotation != null) {
            return MessageProvider.run(templateAnnotation.value, title, throwable)
        }

        val body = messageAnnotation.body
        val details = stringArrayToMap(messageAnnotation.details)
        val links = stringArrayToMap(messageAnnotation.links)

        return AlerticornMessage(title, body, details, links, throwable)
    }

    internal fun notify(
        platformAnnotation: Message.Platform?, message: AlerticornMessage, channelAnnotation: Message.Channel?
    ) {
        try {
            NotificationManager.notify(
                platform = platformAnnotation?.value, message = message, destination = channelAnnotation?.value
            )
        } catch (e: MessageFailedToSendException) {
            println(e.stackTraceToString())
        }
    }

    companion object {
        /**
         * Maps a [TestResult] to the corresponding Alerticorn [Event](s).
         *
         * Returns a list because `TestResult.Error` maps to both `FAIL` and `EXCEPTION`.
         */
        fun mapResultToEvents(result: TestResult): List<Event> {
            return when (result) {
                is TestResult.Success -> listOf(Event.PASS)
                is TestResult.Failure -> listOf(Event.FAIL)
                is TestResult.Error -> listOf(Event.FAIL, Event.EXCEPTION)
                is TestResult.Ignored -> listOf(Event.SKIP, Event.DISABLED)
            }
        }
    }
}
