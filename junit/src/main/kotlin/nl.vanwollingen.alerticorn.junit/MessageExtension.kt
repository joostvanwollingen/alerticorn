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

class MessageExtension : TestExecutionExceptionHandler, TestWatcher {

    override fun testDisabled(context: ExtensionContext?, reason: Optional<String>?) {
        context?.let { handleEvent(it, Event.DISABLED, null) }
    }

    override fun testAborted(context: ExtensionContext?, cause: Throwable?) {
        context?.let { handleEvent(it, Event.ABORTED, null) }
    }

    override fun testSuccessful(context: ExtensionContext) {
        handleEvent(context, Event.PASS, null)
    }

    override fun testFailed(context: ExtensionContext?, cause: Throwable?) {
        context?.let { handleEvent(it, Event.FAIL, cause) }
    }

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


    fun stringArrayToMap(stringArray: Array<String>): Map<String, String> {
        return stringArray.asSequence().chunked(2).associate { it[0] to it.getOrElse(1) { "" } }
    }
}