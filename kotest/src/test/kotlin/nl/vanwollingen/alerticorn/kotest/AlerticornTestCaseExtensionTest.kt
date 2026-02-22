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
import kotlin.test.assertSame
import kotlin.time.Duration.Companion.milliseconds

class AlerticornTestCaseExtensionTest {

    @BeforeEach
    fun setUp() {
        mockkStatic(NotificationManager::class)
        every {
            NotificationManager.notify(platform = any(), message = any(), destination = any())
        } just Runs
    }

    @AfterEach
    fun afterEach() {
        unmockkAll()
    }

    @Test
    fun `sends notification based on configured events`() = runBlocking {
        val ext = AlerticornTestCaseExtension(
            title = "Test failed",
            events = listOf(Event.FAIL),
            platform = "slack",
            channel = "alerts",
        )

        val testCase = createTestCase()
        val failResult = TestResult.Failure(100.milliseconds, AssertionError("assertion"))

        val result = ext.intercept(testCase) { failResult }

        verify(exactly = 1) {
            NotificationManager.notify(platform = "slack", message = any(), destination = "alerts")
        }
        assertSame(failResult, result)
    }

    @Test
    fun `does NOT send when event is not configured`() = runBlocking {
        val ext = AlerticornTestCaseExtension(
            title = "Test failed",
            events = listOf(Event.FAIL),
            platform = "slack",
            channel = "alerts",
        )

        val testCase = createTestCase()
        val successResult = TestResult.Success(100.milliseconds)

        val result = ext.intercept(testCase) { successResult }

        verify(exactly = 0) {
            NotificationManager.notify(platform = any(), message = any(), destination = any())
        }
        assertSame(successResult, result)
    }

    @Test
    fun `empty events list means all events trigger`() = runBlocking {
        val ext = AlerticornTestCaseExtension(
            title = "All events",
            events = emptyList(),
            platform = "slack",
            channel = "alerts",
        )

        val testCase = createTestCase()
        val successResult = TestResult.Success(100.milliseconds)

        ext.intercept(testCase) { successResult }

        verify(exactly = 1) {
            NotificationManager.notify(platform = "slack", message = any(), destination = "alerts")
        }
    }

    @Test
    fun `constructor params mapped to AlerticornMessage correctly`() = runBlocking {
        val ext = AlerticornTestCaseExtension(
            title = "My title",
            body = "My body",
            details = mapOf("key" to "value"),
            links = mapOf("docs" to "http://docs.example.com"),
            events = listOf(Event.FAIL),
            platform = "discord",
            channel = "dev",
        )

        val testCase = createTestCase()
        val error = AssertionError("assertion failed")
        val failResult = TestResult.Failure(100.milliseconds, error)

        ext.intercept(testCase) { failResult }

        val messageSlot = slot<AlerticornMessage>()
        verify(exactly = 1) {
            NotificationManager.notify(
                platform = "discord",
                message = capture(messageSlot),
                destination = "dev",
            )
        }

        val captured = messageSlot.captured
        assertEquals("My title", captured.title)
        assertEquals("My body", captured.body)
        assertEquals(mapOf("key" to "value"), captured.details)
        assertEquals(mapOf("docs" to "http://docs.example.com"), captured.links)
        assertEquals(error, captured.throwable)
    }

    @Test
    fun `template support works`() = runBlocking {
        val expectedMessage = AlerticornMessage(title = "From template", body = "template body")
        MessageProvider.store("testCaseTemplate") { _, _ -> expectedMessage }

        val ext = AlerticornTestCaseExtension(
            title = "Template title",
            template = "testCaseTemplate",
            platform = "slack",
            channel = "alerts",
        )

        val testCase = createTestCase()
        ext.intercept(testCase) { TestResult.Success(100.milliseconds) }

        val messageSlot = slot<AlerticornMessage>()
        verify(exactly = 1) {
            NotificationManager.notify(platform = "slack", message = capture(messageSlot), destination = "alerts")
        }

        assertEquals(expectedMessage, messageSlot.captured)
    }

    @Test
    fun `original TestResult is returned unmodified`() = runBlocking {
        val ext = AlerticornTestCaseExtension(
            title = "Test",
            events = listOf(Event.PASS),
            platform = "slack",
            channel = "alerts",
        )

        val testCase = createTestCase()
        val originalResult = TestResult.Success(42.milliseconds)

        val result = ext.intercept(testCase) { originalResult }

        assertSame(originalResult, result)
    }

    @Test
    fun `MessageFailedToSendException is caught`(): Unit = runBlocking {
        every {
            NotificationManager.notify(platform = any(), message = any(), destination = any())
        } throws MessageFailedToSendException("send failed")

        val ext = AlerticornTestCaseExtension(
            title = "Test",
            platform = "slack",
            channel = "alerts",
        )

        val testCase = createTestCase()
        val result = TestResult.Success(100.milliseconds)

        // Should not throw
        ext.intercept(testCase) { result }
    }

    // --- Helper ---

    private fun createTestCase(): TestCase {
        val spec = mockk<Spec>(relaxed = true)
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
}
