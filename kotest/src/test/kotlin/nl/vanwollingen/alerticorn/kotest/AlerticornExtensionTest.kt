package nl.vanwollingen.alerticorn.kotest

import io.kotest.core.descriptors.Descriptor
import io.kotest.core.descriptors.DescriptorId
import io.kotest.core.names.TestName
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.core.test.TestType
import io.mockk.*
import kotlinx.coroutines.runBlocking
import nl.vanwollingen.alerticorn.api.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

class AlerticornExtensionTest {

    private lateinit var extension: AlerticornExtension

    @BeforeEach
    fun setUp() {
        extension = AlerticornExtension()
        mockkStatic(NotificationManager::class)
        every {
            NotificationManager.notify(platform = any(), message = any(), destination = any())
        } just Runs
    }

    @AfterEach
    fun afterEach() {
        unmockkAll()
    }

    // --- No annotation ---

    @Test
    fun `does not notify when no Message annotation is present on spec class`() = runBlocking {
        val testCase = createTestCase(UnannotatedSpec::class.java)
        extension.afterAny(testCase, TestResult.Success(100.milliseconds))

        verify(exactly = 0) {
            NotificationManager.notify(platform = any(), message = any(), destination = any())
        }
    }

    // --- PASS ---

    @Test
    fun `TestResult Success sends notification when Event PASS is in events`() = runBlocking {
        val testCase = createTestCase(PassAnnotatedSpec::class.java)
        extension.afterAny(testCase, TestResult.Success(100.milliseconds))

        verify(exactly = 1) {
            NotificationManager.notify(platform = "testplatform", message = any(), destination = "testchannel")
        }
    }

    @Test
    fun `TestResult Success does NOT send when Event PASS is not in events`() = runBlocking {
        val testCase = createTestCase(FailOnlyAnnotatedSpec::class.java)
        extension.afterAny(testCase, TestResult.Success(100.milliseconds))

        verify(exactly = 0) {
            NotificationManager.notify(platform = any(), message = any(), destination = any())
        }
    }

    // --- FAIL ---

    @Test
    fun `TestResult Failure sends notification when Event FAIL is in events`() = runBlocking {
        val testCase = createTestCase(FailOnlyAnnotatedSpec::class.java)
        extension.afterAny(testCase, TestResult.Failure(100.milliseconds, AssertionError("assertion failed")))

        verify(exactly = 1) {
            NotificationManager.notify(platform = "testplatform", message = any(), destination = "testchannel")
        }
    }

    // --- EXCEPTION (Error) ---

    @Test
    fun `TestResult Error sends notification for Event EXCEPTION`() = runBlocking {
        val testCase = createTestCase(ExceptionAnnotatedSpec::class.java)
        extension.afterAny(testCase, TestResult.Error(100.milliseconds, RuntimeException("unexpected")))

        verify(exactly = 1) {
            NotificationManager.notify(platform = "testplatform", message = any(), destination = "testchannel")
        }
    }

    // --- SKIP ---

    @Test
    fun `TestResult Ignored sends notification when Event SKIP is in events`() = runBlocking {
        val testCase = createTestCase(SkipAnnotatedSpec::class.java)
        extension.afterAny(testCase, TestResult.Ignored("disabled"))

        verify(exactly = 1) {
            NotificationManager.notify(platform = "testplatform", message = any(), destination = "testchannel")
        }
    }

    // --- ANY ---

    @Test
    fun `Event ANY triggers for any result type`() = runBlocking {
        val testCase = createTestCase(AnyAnnotatedSpec::class.java)
        extension.afterAny(testCase, TestResult.Success(100.milliseconds))

        verify(exactly = 1) {
            NotificationManager.notify(platform = "testplatform", message = any(), destination = "testchannel")
        }
    }

    @Test
    fun `Event ANY triggers for failure result`() = runBlocking {
        val testCase = createTestCase(AnyAnnotatedSpec::class.java)
        extension.afterAny(testCase, TestResult.Failure(100.milliseconds, AssertionError("fail")))

        verify(exactly = 1) {
            NotificationManager.notify(platform = "testplatform", message = any(), destination = "testchannel")
        }
    }

    // --- No Events annotation (all events trigger) ---

    @Test
    fun `no Message Events annotation triggers notification for all events`() = runBlocking {
        val testCase = createTestCase(NoEventsAnnotatedSpec::class.java)
        extension.afterAny(testCase, TestResult.Success(100.milliseconds))

        verify(exactly = 1) {
            NotificationManager.notify(platform = "testplatform", message = any(), destination = "testchannel")
        }
    }

    // --- Message field mapping ---

    @Test
    fun `annotation fields mapped correctly to AlerticornMessage`() = runBlocking {
        val testCase = createTestCase(FullAnnotatedSpec::class.java)
        val error = RuntimeException("the error")
        extension.afterAny(testCase, TestResult.Error(100.milliseconds, error))

        val messageSlot = slot<AlerticornMessage>()
        verify(exactly = 1) {
            NotificationManager.notify(
                platform = "testplatform",
                message = capture(messageSlot),
                destination = "testchannel",
            )
        }

        val captured = messageSlot.captured
        assertEquals("Full title", captured.title)
        assertEquals("Full body", captured.body)
        assertEquals(mapOf("detail1" to "value1"), captured.details)
        assertEquals(mapOf("link1" to "http://example.com"), captured.links)
        assertEquals("the error", captured.throwable?.message)
    }

    // --- Template ---

    @Test
    fun `Message Template delegates to MessageProvider`() = runBlocking {
        val expectedMessage = AlerticornMessage(title = "From template", body = "template body")
        MessageProvider.store("kotestTemplate") { _, _ -> expectedMessage }

        val testCase = createTestCase(TemplateAnnotatedSpec::class.java)
        extension.afterAny(testCase, TestResult.Success(100.milliseconds))

        val messageSlot = slot<AlerticornMessage>()
        verify(exactly = 1) {
            NotificationManager.notify(platform = "testplatform", message = capture(messageSlot), destination = "testchannel")
        }

        assertEquals(expectedMessage, messageSlot.captured)
    }

    // --- Error handling ---

    @Test
    fun `MessageFailedToSendException is caught and does not propagate`() = runBlocking {
        every {
            NotificationManager.notify(platform = any(), message = any(), destination = any())
        } throws MessageFailedToSendException("send failed")

        val testCase = createTestCase(AnyAnnotatedSpec::class.java)

        // Should not throw
        extension.afterAny(testCase, TestResult.Success(100.milliseconds))
    }

    // --- Event mapping ---

    @Test
    fun `mapResultToEvents returns correct events for each result type`() {
        assertEquals(listOf(Event.PASS), AlerticornExtension.mapResultToEvents(TestResult.Success(100.milliseconds)))
        assertEquals(
            listOf(Event.FAIL),
            AlerticornExtension.mapResultToEvents(TestResult.Failure(100.milliseconds, AssertionError("fail")))
        )
        assertEquals(
            listOf(Event.FAIL, Event.EXCEPTION),
            AlerticornExtension.mapResultToEvents(TestResult.Error(100.milliseconds, RuntimeException("error")))
        )
        assertEquals(
            listOf(Event.SKIP, Event.DISABLED),
            AlerticornExtension.mapResultToEvents(TestResult.Ignored("reason"))
        )
    }

    // --- Helper: create TestCase with a real spec instance for annotation resolution ---

    private fun createTestCase(specClass: Class<out Spec>): TestCase {
        val spec = specClass.getDeclaredConstructor().newInstance()

        val descriptor = mockk<Descriptor.TestDescriptor>(relaxed = true)
        every { descriptor.id } returns DescriptorId("test")

        return TestCase(
            descriptor = descriptor,
            name = TestName("testMethod"),
            spec = spec,
            test = {},
            type = TestType.Test,
        )
    }

    // --- Test annotation classes ---

    class UnannotatedSpec : io.kotest.core.spec.style.FunSpec()

    @Message(title = "Pass test")
    @Message.Events([Event.PASS])
    @Message.Platform("testplatform")
    @Message.Channel("testchannel")
    class PassAnnotatedSpec : io.kotest.core.spec.style.FunSpec()

    @Message(title = "Fail test")
    @Message.Events([Event.FAIL])
    @Message.Platform("testplatform")
    @Message.Channel("testchannel")
    class FailOnlyAnnotatedSpec : io.kotest.core.spec.style.FunSpec()

    @Message(title = "Exception test")
    @Message.Events([Event.EXCEPTION])
    @Message.Platform("testplatform")
    @Message.Channel("testchannel")
    class ExceptionAnnotatedSpec : io.kotest.core.spec.style.FunSpec()

    @Message(title = "Skip test")
    @Message.Events([Event.SKIP])
    @Message.Platform("testplatform")
    @Message.Channel("testchannel")
    class SkipAnnotatedSpec : io.kotest.core.spec.style.FunSpec()

    @Message(title = "Any test")
    @Message.Events([Event.ANY])
    @Message.Platform("testplatform")
    @Message.Channel("testchannel")
    class AnyAnnotatedSpec : io.kotest.core.spec.style.FunSpec()

    @Message(title = "No events test")
    @Message.Platform("testplatform")
    @Message.Channel("testchannel")
    class NoEventsAnnotatedSpec : io.kotest.core.spec.style.FunSpec()

    @Message(
        title = "Full title",
        body = "Full body",
        details = ["detail1", "value1"],
        links = ["link1", "http://example.com"],
    )
    @Message.Events([Event.FAIL, Event.EXCEPTION])
    @Message.Platform("testplatform")
    @Message.Channel("testchannel")
    class FullAnnotatedSpec : io.kotest.core.spec.style.FunSpec()

    @Message(title = "Template title")
    @Message.Events([Event.PASS])
    @Message.Template("kotestTemplate")
    @Message.Platform("testplatform")
    @Message.Channel("testchannel")
    class TemplateAnnotatedSpec : io.kotest.core.spec.style.FunSpec()
}
