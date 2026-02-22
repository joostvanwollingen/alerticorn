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

class AlerticornProjectExtensionTest {

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
    fun `afterProject sends notification with accumulated counts`() = runBlocking {
        val ext = AlerticornProjectExtension(
            title = "Suite done",
            platform = "slack",
            channel = "ci",
            events = listOf(Event.SUITE_COMPLETE),
        )

        val testCase = createTestCase()

        // Simulate test results
        ext.afterAny(testCase, TestResult.Success(100.milliseconds))
        ext.afterAny(testCase, TestResult.Success(100.milliseconds))
        ext.afterAny(testCase, TestResult.Success(100.milliseconds))
        ext.afterAny(testCase, TestResult.Failure(100.milliseconds, AssertionError("fail")))
        ext.afterAny(testCase, TestResult.Error(100.milliseconds, RuntimeException("error")))
        ext.afterAny(testCase, TestResult.Ignored("skipped"))

        ext.afterProject()

        val messageSlot = slot<AlerticornMessage>()
        verify(exactly = 1) {
            NotificationManager.notify(
                platform = "slack",
                message = capture(messageSlot),
                destination = "ci",
            )
        }

        val captured = messageSlot.captured
        assertEquals("Suite done", captured.title)
        assertEquals("6", captured.details?.get("total"))
        assertEquals("3", captured.details?.get("passed"))
        assertEquals("1", captured.details?.get("failed"))
        assertEquals("1", captured.details?.get("errors"))
        assertEquals("1", captured.details?.get("ignored"))
    }

    @Test
    fun `beforeProject sends SUITE_START notification`() = runBlocking {
        val ext = AlerticornProjectExtension(
            title = "Suite starting",
            body = "Tests are running",
            platform = "slack",
            channel = "ci",
            events = listOf(Event.SUITE_START),
        )

        ext.beforeProject()

        val messageSlot = slot<AlerticornMessage>()
        verify(exactly = 1) {
            NotificationManager.notify(
                platform = "slack",
                message = capture(messageSlot),
                destination = "ci",
            )
        }

        assertEquals("Suite starting", messageSlot.captured.title)
        assertEquals("Tests are running", messageSlot.captured.body)
    }

    @Test
    fun `beforeProject does NOT send when SUITE_START is not in events`() = runBlocking {
        val ext = AlerticornProjectExtension(
            title = "Suite done",
            platform = "slack",
            channel = "ci",
            events = listOf(Event.SUITE_COMPLETE),
        )

        ext.beforeProject()

        verify(exactly = 0) {
            NotificationManager.notify(platform = any(), message = any(), destination = any())
        }
    }

    @Test
    fun `afterProject does NOT send when SUITE_COMPLETE is not in events`() = runBlocking {
        val ext = AlerticornProjectExtension(
            title = "Suite start only",
            platform = "slack",
            channel = "ci",
            events = listOf(Event.SUITE_START),
        )

        ext.afterProject()

        verify(exactly = 0) {
            NotificationManager.notify(platform = any(), message = any(), destination = any())
        }
    }

    @Test
    fun `counts are accurate after multiple test results`() = runBlocking {
        val ext = AlerticornProjectExtension(
            title = "Summary",
            platform = "slack",
            channel = "ci",
            events = listOf(Event.SUITE_COMPLETE),
        )

        val testCase = createTestCase()

        // 10 passes, 3 failures, 2 errors, 5 ignored
        repeat(10) { ext.afterAny(testCase, TestResult.Success(10.milliseconds)) }
        repeat(3) { ext.afterAny(testCase, TestResult.Failure(10.milliseconds, AssertionError("f"))) }
        repeat(2) { ext.afterAny(testCase, TestResult.Error(10.milliseconds, RuntimeException("e"))) }
        repeat(5) { ext.afterAny(testCase, TestResult.Ignored("i")) }

        ext.afterProject()

        val messageSlot = slot<AlerticornMessage>()
        verify(exactly = 1) {
            NotificationManager.notify(platform = "slack", message = capture(messageSlot), destination = "ci")
        }

        val details = messageSlot.captured.details!!
        assertEquals("20", details["total"])
        assertEquals("10", details["passed"])
        assertEquals("3", details["failed"])
        assertEquals("2", details["errors"])
        assertEquals("5", details["ignored"])
    }

    @Test
    fun `MessageFailedToSendException is caught`() = runBlocking {
        every {
            NotificationManager.notify(platform = any(), message = any(), destination = any())
        } throws MessageFailedToSendException("send failed")

        val ext = AlerticornProjectExtension(
            title = "Test",
            platform = "slack",
            channel = "ci",
            events = listOf(Event.SUITE_COMPLETE),
        )

        // Should not throw
        ext.afterProject()
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
