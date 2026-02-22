package nl.vanwollingen.alerticorn.junit

import nl.vanwollingen.alerticorn.api.*
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler
import org.junit.jupiter.api.extension.TestWatcher
import org.junit.platform.commons.support.AnnotationSupport
import java.lang.reflect.AnnotatedElement
import java.util.*
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass

/**
 * JUnit 5 extension for integrating Alerticorn notifications with test execution events.
 *
 * This extension listens for various test execution events (e.g., test success, failure, aborted) and sends
 * notifications based on annotations on the test class or method.
 *
 * The extension uses the following annotations to control its behavior:
 * - `@Message`: Defines the title and body of the notification message.
 * - `@Message.Events`: Specifies the events that trigger notifications (e.g., PASS, FAIL).
 * - `@Message.Channel`: Specifies the target channel for the notification.
 * - `@Message.Template`: Specifies the template for generating the notification message.
 * - `@Message.Platform`: Specifies the platform for sending the notification (e.g., Discord, Slack).
 *
 * The extension will attempt to notify when any of the specified events occur, such as a test failure,
 * success, or exception, and will use the provided message template and channel for the notification.
 *
 * @see Message
 */
class MessageExtension : TestExecutionExceptionHandler, TestWatcher {

    /**
     * Called when a test is disabled, triggering a notification if applicable.
     *
     * @param context the extension context
     * @param reason the reason for the test being disabled
     */
    override fun testDisabled(context: ExtensionContext?, reason: Optional<String>?) {
        context?.let { handleEvent(it, Event.DISABLED, null) }
    }

    /**
     * Called when a test is aborted, triggering a notification if applicable.
     *
     * @param context the extension context
     * @param cause the cause of the test abortion
     */
    override fun testAborted(context: ExtensionContext?, cause: Throwable?) {
        context?.let { handleEvent(it, Event.ABORTED, null) }
    }

    /**
     * Called when a test is successful, triggering a notification if applicable.
     *
     * @param context the extension context
     */
    override fun testSuccessful(context: ExtensionContext) {
        handleEvent(context, Event.PASS, null)
    }

    /**
     * Called when a test fails, triggering a notification if applicable.
     *
     * @param context the extension context
     * @param cause the cause of the test failure
     */
    override fun testFailed(context: ExtensionContext?, cause: Throwable?) {
        context?.let { handleEvent(it, Event.FAIL, cause) }
    }

    /**
     * Handles the test execution exception, sending a notification if the exception event is configured.
     *
     * @param context the extension context
     * @param throwable the thrown exception
     * @throws Throwable rethrows the original exception
     */
    @Throws(Throwable::class)
    override fun handleTestExecutionException(context: ExtensionContext, throwable: Throwable) {
        val (messageAnnotation, eventsAnnotation, channelAnnotation, templateAnnotation, platformAnnotation) = getAnnotationDetails(
            context
        )

        if (eventsAnnotation?.value?.contains(Event.EXCEPTION) == true || eventsAnnotation?.value?.contains(Event.ANY) == true) {
            getAlerticornMessage(messageAnnotation, templateAnnotation, throwable)?.let {
                notify(platformAnnotation, it, channelAnnotation)
            }
        }

        throw throwable
    }

    private fun handleEvent(context: ExtensionContext, event: Event, throwable: Throwable?) {
        val (messageAnnotation, eventsAnnotation, channelAnnotation, templateAnnotation, platformAnnotation) = getAnnotationDetails(
            context
        )

        if (eventsAnnotation == null || eventsAnnotation.value.contains(event) || eventsAnnotation.value.contains(Event.ANY)) {
            getAlerticornMessage(messageAnnotation, templateAnnotation, throwable)?.let {
                notify(platformAnnotation, it, channelAnnotation)
            }
        }
    }

    private fun getAnnotationDetails(context: ExtensionContext): AnnotationDetails {
        val clazz = context.testClass.getOrNull()
        val method = context.testMethod.getOrNull()

        val message = getAnnotation(method, clazz, Message::class)
        val events = getAnnotation(method, clazz, Message.Events::class)
        val channel = getAnnotation(method, clazz, Message.Channel::class)
        val template = getAnnotation(method, clazz, Message.Template::class)
        val platform = getAnnotation(method, clazz, Message.Platform::class)

        return AnnotationDetails(message, events, channel, template, platform)
    }

    private data class AnnotationDetails(
        val message: Message? = null,
        val events: Message.Events? = null,
        val channel: Message.Channel? = null,
        val template: Message.Template? = null,
        val platform: Message.Platform? = null,
    )

    private inline fun <reified T : Annotation> getAnnotation(
        primary: AnnotatedElement?, secondary: AnnotatedElement?, annotationClass: KClass<T>
    ): T? {
        return AnnotationSupport.findAnnotation(primary, annotationClass.java).getOrNull()
            ?: AnnotationSupport.findAnnotation(secondary, annotationClass.java).getOrNull()
    }

    private fun notify(
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

    private fun getAlerticornMessage(
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

    /**
     * Converts a string array to a map, where each pair of strings is mapped to a key-value entry.
     *
     * @param stringArray the string array to be converted
     * @return a map of key-value pairs
     */
    fun stringArrayToMap(stringArray: Array<String>): Map<String, String> {
        return nl.vanwollingen.alerticorn.api.stringArrayToMap(stringArray)
    }
}
