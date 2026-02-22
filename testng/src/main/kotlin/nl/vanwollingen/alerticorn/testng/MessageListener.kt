package nl.vanwollingen.alerticorn.testng

import nl.vanwollingen.alerticorn.api.*
import org.testng.ITestListener
import org.testng.ITestResult
import org.testng.ISuite
import org.testng.ISuiteListener
import java.lang.reflect.AnnotatedElement

/**
 * TestNG listener for integrating Alerticorn notifications with test execution events.
 *
 * This listener monitors test execution outcomes (success, failure, skip) and suite
 * lifecycle events (start, finish), sending notifications based on annotations on the
 * test method or class.
 *
 * The listener uses the following annotations to control its behavior:
 * - `@Message`: Defines the title and body of the notification message.
 * - `@Message.Events`: Specifies the events that trigger notifications (e.g., PASS, FAIL).
 * - `@Message.Channel`: Specifies the target channel for the notification.
 * - `@Message.Template`: Specifies the template for generating the notification message.
 * - `@Message.Platform`: Specifies the platform for sending the notification (e.g., Discord, Slack).
 *
 * Registration:
 * - Automatic via SPI (`META-INF/services/org.testng.ITestNGListener`)
 * - Explicit via `@Listeners(MessageListener::class)` on test classes
 * - Via `testng.xml` configuration
 *
 * @see Message
 */
class MessageListener : ITestListener, ISuiteListener {

    // --- ITestListener ---

    override fun onTestSuccess(result: ITestResult) {
        handleEvent(result, Event.PASS)
    }

    override fun onTestFailure(result: ITestResult) {
        handleEvent(result, Event.FAIL)
    }

    override fun onTestSkipped(result: ITestResult) {
        handleEvent(result, Event.SKIP)
    }

    override fun onTestFailedButWithinSuccessPercentage(result: ITestResult) {
        handleEvent(result, Event.FAIL)
    }

    // --- ISuiteListener ---

    override fun onStart(suite: ISuite) {
        handleSuiteEvent(suite, Event.SUITE_START)
    }

    override fun onFinish(suite: ISuite) {
        handleSuiteEvent(suite, Event.SUITE_COMPLETE)
    }

    // --- Internal ---

    internal fun handleEvent(result: ITestResult, event: Event) {
        val method = result.method.constructorOrMethod.method
        val clazz = result.testClass.realClass
        val details = getAnnotationDetails(method, clazz)
        val throwable = result.throwable

        val shouldNotifyForEvent = shouldNotify(details.events, event)
        val shouldNotifyForException =
            throwable != null && event == Event.FAIL && shouldNotify(details.events, Event.EXCEPTION)

        if (shouldNotifyForEvent || shouldNotifyForException) {
            getAlerticornMessage(details.message, details.template, throwable)?.let {
                notify(details.platform, it, details.channel)
            }
        }
    }

    internal fun handleSuiteEvent(suite: ISuite, event: Event) {
        val testClasses = suite.allMethods.map { it.testClass.realClass }.distinct()
        for (clazz in testClasses) {
            val details = getAnnotationDetails(primary = null, secondary = clazz)
            if (shouldNotify(details.events, event)) {
                val message = getAlerticornMessage(details.message, details.template, null)
                    ?.let { enrichWithSuiteData(it, suite, event) }
                message?.let { notify(details.platform, it, details.channel) }
            }
        }
    }

    internal fun getAnnotationDetails(primary: AnnotatedElement?, secondary: AnnotatedElement?): AnnotationDetails {
        // Resolve @Message.Events from the same scope that provided @Message,
        // so a class-level filter (e.g. SUITE_COMPLETE) doesn't bleed into method-level messages.
        val messageScope = if (findAnnotation<Message>(primary) != null) primary else secondary
        val message = findAnnotation<Message>(messageScope)
        val events = findAnnotation<Message.Events>(messageScope)

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
        return findAnnotation<T>(primary)
            ?: findAnnotation<T>(secondary)
    }

    private inline fun <reified T : Annotation> findAnnotation(element: AnnotatedElement?): T? {
        if (element == null) return null
        element.getAnnotation(T::class.java)?.let { return it }
        return element.annotations.firstNotNullOfOrNull { it.annotationClass.java.getAnnotation(T::class.java) }
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

    private fun enrichWithSuiteData(message: AlerticornMessage, suite: ISuite, event: Event): AlerticornMessage {
        if (event != Event.SUITE_COMPLETE) return message

        var passed = 0
        var failed = 0
        var skipped = 0

        suite.results.values.forEach { suiteResult ->
            passed += suiteResult.testContext.passedTests.size()
            failed += suiteResult.testContext.failedTests.size()
            skipped += suiteResult.testContext.skippedTests.size()
        }

        val total = passed + failed + skipped
        val suiteDetails = mutableMapOf(
            "suite" to suite.name,
            "total" to total.toString(),
            "passed" to passed.toString(),
            "failed" to failed.toString(),
            "skipped" to skipped.toString(),
        )

        message.details?.let { suiteDetails.putAll(it) }

        return message.copy(details = suiteDetails)
    }
}
