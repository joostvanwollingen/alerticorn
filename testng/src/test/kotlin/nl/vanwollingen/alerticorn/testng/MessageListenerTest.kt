package nl.vanwollingen.alerticorn.testng

import io.mockk.*
import nl.vanwollingen.alerticorn.api.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testng.IResultMap
import org.testng.ISuite
import org.testng.ISuiteResult
import org.testng.ITestContext
import org.testng.ITestNGMethod
import org.testng.ITestResult
import org.testng.ITestClass
import org.testng.internal.ConstructorOrMethod
import java.lang.reflect.Method
import kotlin.test.assertEquals

class MessageListenerTest {

    private lateinit var listener: MessageListener

    @BeforeEach
    fun setUp() {
        listener = spyk<MessageListener>(recordPrivateCalls = true)
        mockkStatic(NotificationManager::class)
        every {
            NotificationManager.notify(
                platform = any(),
                message = any(),
                destination = any(),
            )
        } just Runs
    }

    @AfterEach
    fun afterEach() {
        unmockkAll()
    }

    // --- No annotation ---

    @Test
    fun `does not notify if no Message annotation is present`() {
        val result = createMockResult(UnannotatedTestClass::class.java, "testMethod")

        listener.onTestSuccess(result)
        listener.onTestFailure(result)
        listener.onTestSkipped(result)

        verify(exactly = 0) {
            NotificationManager.notify(platform = any(), message = any(), destination = any())
        }
    }

    // --- PASS ---

    @Test
    fun `onTestSuccess sends notification when Event PASS is in events`() {
        val result = createMockResult(PassAnnotatedTestClass::class.java, "testMethod")

        listener.onTestSuccess(result)

        verify(exactly = 1) {
            NotificationManager.notify(platform = "testplatform", message = any(), destination = "testchannel")
        }
    }

    @Test
    fun `onTestSuccess does NOT send when Event PASS is not in events`() {
        val result = createMockResult(FailOnlyAnnotatedTestClass::class.java, "testMethod")

        listener.onTestSuccess(result)

        verify(exactly = 0) {
            NotificationManager.notify(platform = any(), message = any(), destination = any())
        }
    }

    // --- FAIL ---

    @Test
    fun `onTestFailure sends notification when Event FAIL is in events`() {
        val result = createMockResult(FailOnlyAnnotatedTestClass::class.java, "testMethod")
        every { result.throwable } returns RuntimeException("test failure")

        listener.onTestFailure(result)

        verify(exactly = 1) {
            NotificationManager.notify(platform = "testplatform", message = any(), destination = "testchannel")
        }
    }

    @Test
    fun `onTestFailure does NOT send when Event FAIL is not in events`() {
        val result = createMockResult(PassAnnotatedTestClass::class.java, "testMethod")
        every { result.throwable } returns RuntimeException("test failure")

        listener.onTestFailure(result)

        verify(exactly = 0) {
            NotificationManager.notify(platform = any(), message = any(), destination = any())
        }
    }

    // --- EXCEPTION ---

    @Test
    fun `onTestFailure sends notification when Event EXCEPTION is in events and throwable is present`() {
        val result = createMockResult(ExceptionAnnotatedTestClass::class.java, "testMethod")
        every { result.throwable } returns RuntimeException("exception occurred")

        listener.onTestFailure(result)

        verify(exactly = 1) {
            NotificationManager.notify(platform = "testplatform", message = any(), destination = "testchannel")
        }
    }

    @Test
    fun `onTestFailure does not send EXCEPTION notification when throwable is null`() {
        val result = createMockResult(ExceptionAnnotatedTestClass::class.java, "testMethod")
        every { result.throwable } returns null

        listener.onTestFailure(result)

        verify(exactly = 0) {
            NotificationManager.notify(platform = any(), message = any(), destination = any())
        }
    }

    // --- SKIP ---

    @Test
    fun `onTestSkipped sends notification when Event SKIP is in events`() {
        val result = createMockResult(SkipAnnotatedTestClass::class.java, "testMethod")

        listener.onTestSkipped(result)

        verify(exactly = 1) {
            NotificationManager.notify(platform = "testplatform", message = any(), destination = "testchannel")
        }
    }

    // --- ANY ---

    @Test
    fun `Event ANY triggers notification for any event`() {
        val result = createMockResult(AnyAnnotatedTestClass::class.java, "testMethod")
        every { result.throwable } returns null

        listener.onTestSuccess(result)
        listener.onTestFailure(result)
        listener.onTestSkipped(result)

        verify(exactly = 3) {
            NotificationManager.notify(platform = "testplatform", message = any(), destination = "testchannel")
        }
    }

    // --- No Events annotation (all events trigger) ---

    @Test
    fun `no Message Events annotation triggers notification for all events`() {
        val result = createMockResult(NoEventsAnnotatedTestClass::class.java, "testMethod")
        every { result.throwable } returns null

        listener.onTestSuccess(result)
        listener.onTestFailure(result)
        listener.onTestSkipped(result)

        verify(exactly = 3) {
            NotificationManager.notify(platform = "testplatform", message = any(), destination = "testchannel")
        }
    }

    // --- Message field mapping ---

    @Test
    fun `annotation fields are correctly mapped to AlerticornMessage`() {
        val result = createMockResult(FullAnnotatedTestClass::class.java, "testMethod")
        every { result.throwable } returns RuntimeException("the error")

        listener.onTestFailure(result)

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

    // --- Method overrides class ---

    @Test
    fun `method-level annotations override class-level annotations`() {
        val result = createMockResult(
            ClassWithMethodOverride::class.java,
            "methodWithOverride"
        )
        every { result.throwable } returns null

        listener.onTestSuccess(result)

        val messageSlot = slot<AlerticornMessage>()
        verify(exactly = 1) {
            NotificationManager.notify(
                platform = "methodplatform",
                message = capture(messageSlot),
                destination = "methodchannel",
            )
        }

        assertEquals("Method title", messageSlot.captured.title)
    }

    // --- Template ---

    @Test
    fun `Message Template delegates to MessageProvider`() {
        val expectedMessage = AlerticornMessage(title = "From template", body = "template body")
        MessageProvider.store("myTemplate") { _, _ -> expectedMessage }

        val result = createMockResult(TemplateAnnotatedTestClass::class.java, "testMethod")
        every { result.throwable } returns null

        listener.onTestSuccess(result)

        val messageSlot = slot<AlerticornMessage>()
        verify(exactly = 1) {
            NotificationManager.notify(platform = "testplatform", message = capture(messageSlot), destination = "testchannel")
        }

        assertEquals(expectedMessage, messageSlot.captured)
    }

    // --- onTestFailedButWithinSuccessPercentage ---

    @Test
    fun `onTestFailedButWithinSuccessPercentage maps to FAIL event`() {
        val result = createMockResult(FailOnlyAnnotatedTestClass::class.java, "testMethod")
        every { result.throwable } returns RuntimeException("within percentage")

        listener.onTestFailedButWithinSuccessPercentage(result)

        verify(exactly = 1) {
            NotificationManager.notify(platform = "testplatform", message = any(), destination = "testchannel")
        }
    }

    // --- Suite events ---

    @Test
    fun `suite onFinish sends notification for classes with SUITE_COMPLETE event`() {
        val suite = createMockSuite(SuiteAnnotatedTestClass::class.java, passed = 5, failed = 1, skipped = 2)

        listener.onFinish(suite)

        val messageSlot = slot<AlerticornMessage>()
        verify(exactly = 1) {
            NotificationManager.notify(
                platform = "testplatform",
                message = capture(messageSlot),
                destination = "testchannel",
            )
        }

        val captured = messageSlot.captured
        assertEquals("Suite done", captured.title)
        assertEquals("8", captured.details?.get("total"))
        assertEquals("5", captured.details?.get("passed"))
        assertEquals("1", captured.details?.get("failed"))
        assertEquals("2", captured.details?.get("skipped"))
        assertEquals("Test Suite", captured.details?.get("suite"))
    }

    @Test
    fun `suite onStart sends SUITE_START notification`() {
        val suite = createMockSuite(SuiteStartAnnotatedTestClass::class.java, passed = 0, failed = 0, skipped = 0)

        listener.onStart(suite)

        verify(exactly = 1) {
            NotificationManager.notify(
                platform = "testplatform",
                message = any(),
                destination = "testchannel",
            )
        }
    }

    // --- Error handling ---

    @Test
    fun `MessageFailedToSendException is caught and does not propagate`() {
        every {
            NotificationManager.notify(platform = any(), message = any(), destination = any())
        } throws MessageFailedToSendException("send failed")

        val result = createMockResult(AnyAnnotatedTestClass::class.java, "testMethod")
        every { result.throwable } returns null

        // Should not throw
        listener.onTestSuccess(result)
    }

    // --- Helper: create mock ITestResult ---

    private fun createMockResult(testClass: Class<*>, methodName: String): ITestResult {
        val result = mockk<ITestResult>(relaxed = true)

        val method: Method = try {
            testClass.getMethod(methodName)
        } catch (e: NoSuchMethodException) {
            testClass.getDeclaredMethod(methodName)
        }

        val constructorOrMethod = mockk<ConstructorOrMethod>()
        every { constructorOrMethod.method } returns method

        val testNGMethod = mockk<ITestNGMethod>()
        every { testNGMethod.constructorOrMethod } returns constructorOrMethod

        val testClassMock = mockk<ITestClass>()
        every { testClassMock.realClass } returns testClass

        every { result.method } returns testNGMethod
        every { result.testClass } returns testClassMock
        every { result.throwable } returns null
        every { testNGMethod.testClass } returns testClassMock

        return result
    }

    // --- Helper: create mock ISuite ---

    private fun createMockSuite(testClass: Class<*>, passed: Int, failed: Int, skipped: Int): ISuite {
        val suite = mockk<ISuite>(relaxed = true)
        every { suite.name } returns "Test Suite"

        val testNGMethod = mockk<ITestNGMethod>()
        val testClassMock = mockk<ITestClass>()
        every { testClassMock.realClass } returns testClass
        every { testNGMethod.testClass } returns testClassMock

        every { suite.allMethods } returns listOf(testNGMethod)

        val passedResults = mockk<IResultMap>()
        every { passedResults.size() } returns passed
        val failedResults = mockk<IResultMap>()
        every { failedResults.size() } returns failed
        val skippedResults = mockk<IResultMap>()
        every { skippedResults.size() } returns skipped

        val testContext = mockk<ITestContext>()
        every { testContext.passedTests } returns passedResults
        every { testContext.failedTests } returns failedResults
        every { testContext.skippedTests } returns skippedResults

        val suiteResult = mockk<ISuiteResult>()
        every { suiteResult.testContext } returns testContext

        every { suite.results } returns mapOf("test" to suiteResult)

        return suite
    }

    // --- Test annotation classes ---

    class UnannotatedTestClass {
        fun testMethod() {}
    }

    @Message(title = "Pass test")
    @Message.Events([Event.PASS])
    @Message.Platform("testplatform")
    @Message.Channel("testchannel")
    class PassAnnotatedTestClass {
        fun testMethod() {}
    }

    @Message(title = "Fail test")
    @Message.Events([Event.FAIL])
    @Message.Platform("testplatform")
    @Message.Channel("testchannel")
    class FailOnlyAnnotatedTestClass {
        fun testMethod() {}
    }

    @Message(title = "Exception test")
    @Message.Events([Event.EXCEPTION])
    @Message.Platform("testplatform")
    @Message.Channel("testchannel")
    class ExceptionAnnotatedTestClass {
        fun testMethod() {}
    }

    @Message(title = "Skip test")
    @Message.Events([Event.SKIP])
    @Message.Platform("testplatform")
    @Message.Channel("testchannel")
    class SkipAnnotatedTestClass {
        fun testMethod() {}
    }

    @Message(title = "Any test")
    @Message.Events([Event.ANY])
    @Message.Platform("testplatform")
    @Message.Channel("testchannel")
    class AnyAnnotatedTestClass {
        fun testMethod() {}
    }

    @Message(title = "No events test")
    @Message.Platform("testplatform")
    @Message.Channel("testchannel")
    class NoEventsAnnotatedTestClass {
        fun testMethod() {}
    }

    @Message(
        title = "Full title",
        body = "Full body",
        details = ["detail1", "value1"],
        links = ["link1", "http://example.com"],
    )
    @Message.Events([Event.FAIL])
    @Message.Platform("testplatform")
    @Message.Channel("testchannel")
    class FullAnnotatedTestClass {
        fun testMethod() {}
    }

    @Message(title = "Class title")
    @Message.Events([Event.PASS])
    @Message.Platform("classplatform")
    @Message.Channel("classchannel")
    class ClassWithMethodOverride {
        @Message(title = "Method title")
        @Message.Events([Event.PASS])
        @Message.Platform("methodplatform")
        @Message.Channel("methodchannel")
        fun methodWithOverride() {
        }
    }

    @Message(title = "Template title")
    @Message.Events([Event.PASS])
    @Message.Template("myTemplate")
    @Message.Platform("testplatform")
    @Message.Channel("testchannel")
    class TemplateAnnotatedTestClass {
        fun testMethod() {}
    }

    @Message(title = "Suite done")
    @Message.Events([Event.SUITE_COMPLETE])
    @Message.Platform("testplatform")
    @Message.Channel("testchannel")
    class SuiteAnnotatedTestClass {
        fun testMethod() {}
    }

    @Message(title = "Suite starting")
    @Message.Events([Event.SUITE_START])
    @Message.Platform("testplatform")
    @Message.Channel("testchannel")
    class SuiteStartAnnotatedTestClass {
        fun testMethod() {}
    }
}
