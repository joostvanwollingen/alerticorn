package nl.vanwollingen.alerticorn.api

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RunWithTest {

    @Test
    fun `given a message builder, when called, it returns the result and sends the message`() {
        val builder: (m: String, v: String) -> AlerticornMessage = { m, v -> AlerticornMessage("this is the $m", v) }

        val s = RunWith.message("fake", "somewhere", "ze message", builder) {
            "the result"
        }

        val notifier = NotificationManager.getNotifier("fake")!! as FakeNotifier
        val message = notifier.calls["somewhere"]

        assertEquals("the result", s)
        assertEquals("this is the ze message", message?.title)
        assertEquals("the result", message?.body)
    }

    @Test
    fun `given a message builder, when an exception is thrown, it sends the message and rethrows`() {
        val rethrown = assertThrows<Error>() {
            RunWith.messageOnException(
                "fake",
                "somewhere",
                "Something failed",
                { m, v -> AlerticornMessage(m, body = v.message.toString(), throwable = v) }) {
                throw Error("Fail")
            }
        }

        val notifier = NotificationManager.getNotifier("fake")!! as FakeNotifier
        val message = notifier.calls["somewhere"]

        assertEquals("Something failed", message?.title)
        assertEquals("Fail", message?.body)
        assertEquals("Fail", rethrown.message)
    }
}