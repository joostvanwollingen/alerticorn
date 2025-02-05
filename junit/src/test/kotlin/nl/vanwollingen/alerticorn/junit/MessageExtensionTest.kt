package nl.vanwollingen.alerticorn.junit

import io.mockk.*
import nl.vanwollingen.alerticorn.api.AlerticornMessage
import nl.vanwollingen.alerticorn.api.Event
import nl.vanwollingen.alerticorn.api.Message
import nl.vanwollingen.alerticorn.api.NotificationManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.support.AnnotationSupport
import org.junitpioneer.jupiter.SetEnvironmentVariable
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import java.util.*
import kotlin.test.assertEquals

class MessageExtensionTest {

    private lateinit var handler: MessageExtension

    @BeforeEach
    fun setUp() {
        handler = spyk<MessageExtension>(recordPrivateCalls = true)
        mockkStatic(AnnotationSupport::class)
        mockkObject(NotificationManager)
    }

    @Test
    fun `does not notify if no context is passed`() {
        handler.testFailed(context = null, null)
        handler.testDisabled(context = null, reason = Optional.of("Disabled"))
        handler.testAborted(context = null, null)

        verify(exactly = 0) {
            NotificationManager.notify(
                platform = any(),
                message = any(),
                destination = any(),
            )
        }
    }

    @Test
    fun `does not notify if no message title is present`() {
        val (context, _) = setupAnnotationMocks(false)

        handler.testFailed(context = context, null)
        handler.testDisabled(context = context, reason = Optional.of("Disabled"))
        handler.testAborted(context = context, null)
        handler.testSuccessful(context = context)

        verify(exactly = 0) {
            NotificationManager.notify(
                platform = any(),
                message = any(),
                destination = any(),
            )
        }
    }

    @Test
    fun `testDisabled does not notify if Event Disabled is not present`() {
        val (context, annotations) = setupAnnotationMocks()
        val messageEventsAnnotationMock: Message.Events = annotations[1] as Message.Events

        mockMessageAnnotations(
            annotations[0] as Message,
            annotations[3] as Message.Channel,
            annotations[2] as Message.Platform
        )

        every { messageEventsAnnotationMock.value } returns arrayOf(Event.SKIP)

        handler.testDisabled(context = context, reason = Optional.of("Disabled"))

        verify(exactly = 1) {
            handler["handleEvent"](any<ExtensionContext>(), Event.DISABLED, any<Throwable>())
        }

        verify(exactly = 0) {
            NotificationManager.notify(
                platform = any(),
                message = any(),
                destination = any(),
            )
        }
    }

    @Test
    fun `testDisabled does notify if Event disabled is annotated`() {
        val (context, annotations) = setupAnnotationMocks()
        val messageEventsAnnotationMock: Message.Events = annotations[1] as Message.Events

        mockMessageAnnotations(
            annotations[0] as Message,
            annotations[3] as Message.Channel,
            annotations[2] as Message.Platform,
            details = null,
            links = null,
        )
        every { messageEventsAnnotationMock.value } returns arrayOf(Event.DISABLED)

        handler.testDisabled(context = context, reason = Optional.of("Disabled"))

        verify(exactly = 1) {
            handler["handleEvent"](any<ExtensionContext>(), Event.DISABLED, any<Throwable>())

            NotificationManager.notify(
                platform = any(),
                message = any(),
                destination = any(),
            )
        }
    }

    @Test
    fun `testSuccessful does not notify if Event Pass is not present`() {
        val (context, annotations) = setupAnnotationMocks()
        val messageEventsAnnotationMock: Message.Events = annotations[1] as Message.Events

        mockMessageAnnotations(
            annotations[0] as Message,
            annotations[3] as Message.Channel,
            annotations[2] as Message.Platform
        )

        every { messageEventsAnnotationMock.value } returns arrayOf(Event.SKIP)

        handler.testSuccessful(context = context)

        verify(exactly = 1) {
            handler["handleEvent"](any<ExtensionContext>(), Event.PASS, any<Throwable>())
        }

        verify(exactly = 0) {
            NotificationManager.notify(
                platform = any(),
                message = any(),
                destination = any(),
            )
        }
    }

    @Test
    fun `testDisabled does notify if Event Any is annotated`() {
        val (context, annotations) = setupAnnotationMocks()
        val messageEventsAnnotationMock: Message.Events = annotations[1] as Message.Events

        mockMessageAnnotations(
            annotations[0] as Message,
            annotations[3] as Message.Channel,
            annotations[2] as Message.Platform,
            details = null,
            links = null,
        )
        every { messageEventsAnnotationMock.value } returns arrayOf(Event.ANY)

        handler.testDisabled(context = context, reason = Optional.of("Disabled"))

        verify(exactly = 1) {
            handler["handleEvent"](any<ExtensionContext>(), Event.DISABLED, any<Throwable>())

            NotificationManager.notify(
                platform = any(),
                message = any(),
                destination = any(),
            )
        }
    }

    @Test
    fun `testSuccessful does notify if Event Pass is annotated`() {
        val (context, annotations) = setupAnnotationMocks()
        val messageEventsAnnotationMock: Message.Events = annotations[1] as Message.Events

        mockMessageAnnotations(
            annotations[0] as Message,
            annotations[3] as Message.Channel,
            annotations[2] as Message.Platform,
            details = null,
            links = null,
        )
        every { messageEventsAnnotationMock.value } returns arrayOf(Event.PASS)

        handler.testSuccessful(context = context)

        verify(exactly = 1) {
            handler["handleEvent"](any<ExtensionContext>(), Event.PASS, any<Throwable>())

            NotificationManager.notify(
                platform = any(),
                message = any(),
                destination = any(),
            )
        }
    }

    @Test
    fun `testFailed does not notify if Event Failed is not present`() {
        val (context, annotations) = setupAnnotationMocks()
        val messageEventsAnnotationMock: Message.Events = annotations[1] as Message.Events

        mockMessageAnnotations(
            annotations[0] as Message,
            annotations[3] as Message.Channel,
            annotations[2] as Message.Platform
        )

        every { messageEventsAnnotationMock.value } returns arrayOf(Event.SKIP)

        handler.testFailed(context = context, Error("failed"))

        verify(exactly = 1) {
            handler["handleEvent"](any<ExtensionContext>(), Event.FAIL, any<Throwable>())
        }

        verify(exactly = 0) {
            NotificationManager.notify(
                platform = any(),
                message = any(),
                destination = any(),
            )
        }
    }

    @Test
    fun `testFailed does notify if Event Failed is  present`() {
        val (context, annotations) = setupAnnotationMocks()
        val messageEventsAnnotationMock: Message.Events = annotations[1] as Message.Events

        mockMessageAnnotations(
            annotations[0] as Message,
            annotations[3] as Message.Channel,
            annotations[2] as Message.Platform
        )

        every { messageEventsAnnotationMock.value } returns arrayOf(Event.FAIL)

        handler.testFailed(context = context, Error("failed"))

        verify(exactly = 1) {
            handler["handleEvent"](any<ExtensionContext>(), Event.FAIL, any<Throwable>())

            NotificationManager.notify(
                platform = any(),
                message = any(),
                destination = any(),
            )
        }
    }

    @Test
    fun `testAborted does not notify if Event Aborted is not present`() {
        val (context, annotations) = setupAnnotationMocks()
        val messageEventsAnnotationMock: Message.Events = annotations[1] as Message.Events

        mockMessageAnnotations(
            annotations[0] as Message,
            annotations[3] as Message.Channel,
            annotations[2] as Message.Platform
        )

        every { messageEventsAnnotationMock.value } returns arrayOf(Event.SKIP)

        handler.testAborted(context = context, Error("failed"))

        verify(exactly = 1) {
            handler["handleEvent"](any<ExtensionContext>(), Event.ABORTED, any<Throwable>())
        }

        verify(exactly = 0) {
            NotificationManager.notify(
                platform = any(),
                message = any(),
                destination = any(),
            )
        }
    }

    @Test
    fun `testAborted does notify if Event Aborted is  present`() {
        val (context, annotations) = setupAnnotationMocks()
        val messageEventsAnnotationMock: Message.Events = annotations[1] as Message.Events

        mockMessageAnnotations(
            annotations[0] as Message,
            annotations[3] as Message.Channel,
            annotations[2] as Message.Platform
        )

        every { messageEventsAnnotationMock.value } returns arrayOf(Event.ABORTED)

        handler.testAborted(context = context, Error("failed"))

        verify(exactly = 1) {
            handler["handleEvent"](any<ExtensionContext>(), Event.ABORTED, any<Throwable>())

            NotificationManager.notify(
                platform = any(),
                message = any(),
                destination = any(),
            )
        }
    }

    @SetEnvironmentVariable(key = "CHANNEL_1", value = "general")
    @Test
    fun `handleTestExecutionException maps annotation fields to the message`() {
        val (context, annotations) = setupAnnotationMocks()
        val messageAnnotationMock: Message = annotations[0] as Message
        val messageEventsAnnotationMock: Message.Events = annotations[1] as Message.Events
        val messagePlatformAnnotationMock: Message.Platform = annotations[2] as Message.Platform
        val messageChannelAnnotationMock: Message.Channel = annotations[3] as Message.Channel

        mockMessageAnnotations(
            messageAnnotationMock,
            messageChannelAnnotationMock,
            messagePlatformAnnotationMock,
            details = arrayOf("detail1", "detailed"),
            links = arrayOf("documentation", "http://here")
        )
        every { messageEventsAnnotationMock.value } returns arrayOf(Event.EXCEPTION)

        // Execute the handler
        val throwable = RuntimeException("Test failure")
        val exception = assertThrows<Throwable> {
            handler.handleTestExecutionException(context, throwable)
        }

        val alerticornMessageSlot = slot<AlerticornMessage>()

        // Verify we called the notification manager
        verify(exactly = 1) {
            NotificationManager.notify(
                platform = "alerticorn",
                message = capture(alerticornMessageSlot),
                destination = "CHANNEL_1",
            )
        }

        //Verify the message
        val capturedMessage = alerticornMessageSlot.captured
        assertEquals(messageAnnotationMock.title, capturedMessage.title)
        assertEquals(messageAnnotationMock.body, capturedMessage.body)
        assertEquals(mapOf("detail1" to "detailed"), capturedMessage.details)
        assertEquals(mapOf("documentation" to "http://here"), capturedMessage.links)
        assertEquals(throwable, capturedMessage.throwable)

        // Ensure the original exception is rethrown
        assertEquals(throwable, exception)
    }

    private fun mockMessageAnnotations(
        messageAnnotationMock: Message,
        messageChannelAnnotationMock: Message.Channel,
        messagePlatformAnnotationMock: Message.Platform,
        title: String = "A message for control",
        body: String = "Houston we got a problem",
        channel: String = "CHANNEL_1",
        platform: String = "alerticorn",
        details: Array<String>? = null,
        links: Array<String>? = null,
    ) {
        every { messageAnnotationMock.title } returns title
        every { messageAnnotationMock.body } returns body
        every { messageChannelAnnotationMock.value } returns channel
        every { messagePlatformAnnotationMock.value } returns platform
        every { messageAnnotationMock.details } returns (details ?: emptyArray())
        every { messageAnnotationMock.links } returns (links ?: emptyArray())
    }

    @Test
    fun `it should convert a string array to a map`() {
        val stringArray = arrayOf("runbook", "http://bla")
        val map = MessageExtension().stringArrayToMap(stringArray)
        assertEquals(map["runbook"], "http://bla")
    }

    @Test
    fun `it should convert an unbalanced string array to a map`() {
        val stringArray = arrayOf("runbook", "http://bla", "another")
        val map = MessageExtension().stringArrayToMap(stringArray)
        assertEquals(map["runbook"], "http://bla")
        assertEquals(map["another"], "")
    }

    private fun setupAnnotationMocks(present: Boolean = true): Pair<ExtensionContext, List<Annotation>> {
        val messageAnnotationMock = mockk<Message>()
        val messageEventsAnnotationMock = mockk<Message.Events>()
        val messagePlatformAnnotationMock = mockk<Message.Platform>()
        val messageChannelAnnotationMock = mockk<Message.Channel>()
        val messageTemplateAnnotationMock = mockk<Message.Template>()

        val context = mockk<ExtensionContext>()

        val mockedMethod: Method = mockk()
        every { context.testMethod } returns Optional.of(mockedMethod)

        val mockedClass: MyTestClass = mockk()
        every { context.testClass } returns Optional.of(mockedClass::class.java)

        // Mock the context's element to return the annotation
        every { AnnotationSupport.findAnnotation(any<AnnotatedElement>(), Message::class.java) } returns  if(present) Optional.of(
            messageAnnotationMock
        ) else Optional.empty()

        every {
            AnnotationSupport.findAnnotation(
                any<AnnotatedElement>(),
                Message.Events::class.java
            )
        } returns if (present) Optional.of(messageEventsAnnotationMock) else Optional.empty()

        every {
            AnnotationSupport.findAnnotation(
                any<AnnotatedElement>(),
                Message.Platform::class.java
            )
        } returns if (present) Optional.of(messagePlatformAnnotationMock) else Optional.empty()

        every {
            AnnotationSupport.findAnnotation(
                any<AnnotatedElement>(),
                Message.Channel::class.java
            )
        } returns if (present) Optional.of(messageChannelAnnotationMock) else Optional.empty()

        every {
            AnnotationSupport.findAnnotation(
                any<AnnotatedElement>(),
                Message.Template::class.java
            )
        } returns Optional.empty()

        return context to listOf(
            messageAnnotationMock,
            messageEventsAnnotationMock,
            messagePlatformAnnotationMock,
            messageChannelAnnotationMock,
            messageTemplateAnnotationMock,
        )
    }

    @AfterEach
    fun afterEach() {
        unmockkAll()
    }

    class MyTestClass
}
